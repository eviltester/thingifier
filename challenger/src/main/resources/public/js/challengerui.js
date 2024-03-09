function setCookie(cname,cvalue,exdays) {
  let expires="";
  if(exdays!=undefined){ // use undefined to create a session cookie
      const d = new Date();
      d.setTime(d.getTime() + (exdays*24*60*60*1000));
      expires = 'expires=' + d.toUTCString() + ";";
  }
  document.cookie = cname + '=' + cvalue + ';' + expires + 'path=/';
}

function getCookie(cname) {
  let name = cname + '=';
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
}

function inputChallengeGuid(){
    let guid = prompt('Input a Challenger GUID to use');
    if(guid){location.href=`/gui/challenges/`+encodeURIComponent(guid);};
}

function forgetGuid(aguid){
    var guids = localStorage.getItem('challenges-guids');
    guids = guids.replace(`|${aguid}|`, '');
    localStorage.setItem('challenges-guids', guids);
    localStorage.removeItem(`${aguid}.data`);
    localStorage.removeItem(`${aguid}.progress`);
    document.getElementById('p'+aguid).remove();
    if(getCookie('X-THINGIFIER-DATABASE-NAME')== aguid){
        setCookie('X-THINGIFIER-DATABASE-NAME','',0);
    }
}

function displayLocalGuids(){
    var guids = localStorage.getItem('challenges-guids') || '';
    var guidsArray = guids.match(/\|([^|]*)\|/g);
    currGuid = getCookie('X-THINGIFIER-DATABASE-NAME');
    if(currGuid && !guidsArray){
        guidsArray=[];
    }
    if(currGuid && guidsArray && currGuid!='' && !guidsArray.includes(`|${currGuid}|`)){
        guidsArray.push(`|${currGuid}|`)
    }
    if(guidsArray!=null && guidsArray.length>0){
        document.writeln('<details><summary>Previously Used Challenger GUIDs</summary>')

        for(guidItem in guidsArray){
            var myguid = guidsArray[guidItem].replace(/\|/g,'');
            document.writeln("<p id='p" + myguid + "'>");
            document.writeln("<a href='/gui/challenges/"+myguid+"'>"+myguid+"</a>");
            document.writeln("&nbsp;<button onclick=forgetGuid('"+myguid+"')>forget</button>");
            document.writeln("</p>");
        }
        document.writeln('</details>');
    }
}

function setCookieSaveLocally(){
    setCookie("auto-save-x-challenger-locally","true");
    const challengerData = document.challengerData;
    if(challengerData && challengerData.xChallenger){
        currentChallengerGuid = challengerData.xChallenger;
        setCookie("last-auto-saved-x-challenger", currentChallengerGuid);
    }
}

function deleteCookieSaveLocally(){
    setCookie("auto-save-x-challenger-locally","")
}

function showCurrentStatus(){
    const challengerData = document.challengerData;
    const databaseData = document.databaseData;

    document.writeln("<div>");

    document.writeln(`<p><button onclick=location.reload()>Refresh Status</button>`);

    // issue if immediately autosave then it will wipe out any saved
    // use a session cookie to force setting for everyone
    // and have a last-auto-saved-guid and only switch on auto save if current guid matches last autosaved
    var lastAutoSavedGuid = getCookie("last-auto-saved-x-challenger")
    var currentChallengerGuid = "";
    var canAutoSaveChallenger = false;
    if(challengerData && challengerData.xChallenger){
        currentChallengerGuid = challengerData.xChallenger;
        if(currentChallengerGuid!="" && currentChallengerGuid===lastAutoSavedGuid){
            canAutoSaveChallenger=true;
        }
    }

    var autoSave = false;
    if(getCookie("auto-save-x-challenger-locally")!='' && canAutoSaveChallenger){
        autoSave=true;
    }

    var autoSaveOn = autoSave ? "checked" : ""
    var autoSaveOnChange = autoSave ? "'deleteCookieSaveLocally();location.reload();'" : "'setCookieSaveLocally();location.reload()'"
    var autoSaveLocallyHtml = ` <input type='checkbox' id='auto-save-locally-check' name='auto-save-locally-check' value='auto-save-locally-check' ${autoSaveOn} onchange=${autoSaveOnChange}><label for='auto-save-locally-check'> Auto Save Todos and Progress Locally on refresh</label>`
    document.writeln(autoSaveLocallyHtml);

    document.writeln("</p>");


    if(challengerData && challengerData.xChallenger){
        var xChallengerGuid = challengerData.xChallenger;


        if(challengerData.challengeStatus){
            var status = challengerData.challengeStatus;
            var doneCount = Object.values(challengerData.challengeStatus).filter(x=>x).length;
            var totalCount = Object.values(challengerData.challengeStatus).length;
            var leftCount = totalCount - doneCount;
            document.writeln(`<p>${totalCount} Challenges: ${doneCount} complete, ${leftCount} remain. <br>`);

            // only allow save if not in localStorage or is different from local storage
            if(localStorage.getItem(`${xChallengerGuid}.progress`)!==JSON.stringify(challengerData)){
                if(!autoSaveOn){
                    document.writeln(`<button onclick="saveChallengerProgressToLocalStorage(challengerData);this.innerText='saved progress';this.setAttribute('disabled',true)">Save Your Progress to LocalStorage</button>`);
                }else{
                    saveChallengerProgressToLocalStorage(challengerData);
                }
            }else{
                document.writeln(`<button disabled>saved progress</button>`);
            }
            if(localStorage.getItem(`${xChallengerGuid}.progress`)){
                document.writeln(`<button onclick="restoreChallengerProgressInSystem('${xChallengerGuid}');this.innerText='restored';this.setAttribute('disabled',true)">Restore Locally Saved Progress</button>`);
            }
            document.writeln(`</p>`);
        }

        if(databaseData && databaseData.todos){

            document.writeln(`<p>${databaseData.todos.length} todos in database. `);
            document.writeln(`<a href='/gui/instances?entity=todo'>View Todos</a> <br>`)

            // only allow save button if not in localStorage or is different from local storage
            document.databaseData.todos.sort((a,b)=>a.id-b.id);
            if(localStorage.getItem(`${xChallengerGuid}.data`)!==JSON.stringify(document.databaseData)){
                if(!autoSaveOn){
                    document.writeln(`<button onclick="saveChallengerTodosToLocalStorage(databaseData,challengerData);this.innerText='saved todos';this.setAttribute('disabled',true)">Save Your Todos Data to LocalStorage</button>`);
                }else{
                    saveChallengerTodosToLocalStorage(databaseData,challengerData);
                }
            }else{
                document.writeln(`<button disabled>saved todos</button>`);
            }
            if(localStorage.getItem(`${xChallengerGuid}.data`)){
                document.writeln(`<button onclick="restoreTodosInSystem('${xChallengerGuid}');this.innerText='restored';this.setAttribute('disabled',true)">Restore Locally Saved Todos Data</button>`);
            }
            document.writeln(`</p>`);
        }else{
            if(localStorage.getItem(`${xChallengerGuid}.data`)){
                document.writeln(`<button onclick="restoreTodosInSystem('${xChallengerGuid}')">Restore Locally Saved Todos Data</button>`);
            }
        }
    }else{
        // if we have a guid in the url then allow restoring
        var parts = location.pathname.split("/");
        var possibleUuid = parts[parts.length-1];

        if(localStorage.getItem(`${possibleUuid}.progress`) || localStorage.getItem(`${possibleUuid}.data`)){

            document.writeln(`<p><strong>Restore Data for ${possibleUuid}</strong></p>`);
            if(localStorage.getItem(`${possibleUuid}.progress`)){
                document.writeln(`<button onclick="restoreChallengerProgressInSystem('${possibleUuid}')">Restore Locally Saved Progress</button>`);
            }

            if(localStorage.getItem(`${possibleUuid}.data`)){
                document.writeln(`<button onclick="restoreTodosInSystem('${possibleUuid}')">Restore Locally Saved Todos Data</button>`);
            }
        }

    }
    document.writeln("</div>");
    // if we haven't managed to create the challenger yet
}

    // get challenger progress and save to local storage
    // get challenger todos and save to local storage

function saveChallengerProgressToLocalStorage(aChallenger){
    if(aChallenger && aChallenger.xChallenger){
        localStorage.setItem(aChallenger.xChallenger + ".progress", JSON.stringify(aChallenger));
    }
}

function saveChallengerTodosToLocalStorage(data, aChallenger){
    if(data && aChallenger && aChallenger.xChallenger){
        data.todos.sort((a,b)=>a.id-b.id);
        localStorage.setItem(aChallenger.xChallenger + ".data", JSON.stringify(data));
    }
}

function restoreChallengerProgressInSystem(xchallengeruuid){

    data = localStorage.getItem(`${xchallengeruuid}.progress`);
    if(data==null) return;

    fetch(`/challenger/${xchallengeruuid}`, {
      method: "PUT",
      body: data,
      headers: {
        "Content-type": "application/json",
      },
    })
    .then((response) => console.log(response));

}

function restoreTodosInSystem(xchallengeruuid){

    data = localStorage.getItem(`${xchallengeruuid}.data`);
    if(data==null) return;

    fetch(`/challenger/database/${xchallengeruuid}`, {
      method: "PUT",
      body: data,
      headers: {
        "Content-type": "application/json",
      },
    })
    .then((response) => console.log(response));

}