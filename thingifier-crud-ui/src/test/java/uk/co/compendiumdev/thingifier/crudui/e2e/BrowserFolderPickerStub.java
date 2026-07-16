package uk.co.compendiumdev.thingifier.crudui.e2e;

import com.google.gson.Gson;
import com.microsoft.playwright.Page;
import java.util.LinkedHashMap;
import java.util.Map;

final class BrowserFolderPickerStub {

    private final Page page;
    private final Gson gson = new Gson();

    BrowserFolderPickerStub(final Page page) {
        this.page = page;
    }

    void install(final String folderName, final Map<String, String> files) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", folderName);
        payload.put("files", files);
        page.addInitScript(
                "window.__thingifierCrudUiTestDirectory = "
                        + gson.toJson(payload)
                        + ";\n"
                        + "window.showDirectoryPicker = async function() {\n"
                        + "  const directory = window.__thingifierCrudUiTestDirectory;\n"
                        + "  return {\n"
                        + "    name: directory.name,\n"
                        + "    async getFileHandle(name, options) {\n"
                        + "      if (!(name in directory.files) && !(options && options.create)) {\n"
                        + "        throw new Error('Missing browser file: ' + name);\n"
                        + "      }\n"
                        + "      if (!(name in directory.files)) {\n"
                        + "        directory.files[name] = '';\n"
                        + "      }\n"
                        + "      return {\n"
                        + "        async getFile() {\n"
                        + "          const value = directory.files[name] || '';\n"
                        + "          return new File([value], name, {type: 'text/plain'});\n"
                        + "        },\n"
                        + "        async createWritable() {\n"
                        + "          return {\n"
                        + "            async write(value) {\n"
                        + "              if (value instanceof Uint8Array) {\n"
                        + "                let binary = '';\n"
                        + "                for (const byte of value) binary += String.fromCharCode(byte);\n"
                        + "                directory.files[name] = btoa(binary);\n"
                        + "              } else {\n"
                        + "                directory.files[name] = String(value);\n"
                        + "              }\n"
                        + "            },\n"
                        + "            async close() {}\n"
                        + "          };\n"
                        + "        }\n"
                        + "      };\n"
                        + "    }\n"
                        + "  };\n"
                        + "};");
    }

    @SuppressWarnings("unchecked")
    Map<String, String> files() {
        return (Map<String, String>)
                page.evaluate("() => window.__thingifierCrudUiTestDirectory.files");
    }
}
