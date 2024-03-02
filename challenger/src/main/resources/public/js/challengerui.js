function setCookie(cname,cvalue,exdays) {
  const d = new Date();
  d.setTime(d.getTime() + (exdays*24*60*60*1000));
  let expires = 'expires=' + d.toUTCString();
  document.cookie = cname + '=' + cvalue + ';' + expires + ';path=/';
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
        document.writeln('<p><strong>Previously Used</strong></p>')
    }
    for(guidItem in guidsArray){
        var myguid = guidsArray[guidItem].replace(/\|/g,'');
        document.writeln("<p id='p" + myguid + "'>");
        document.writeln("<a href='/gui/challenges/"+myguid+"'>"+myguid+"</a>");
        document.writeln("&nbsp;<button onclick=forgetGuid('"+myguid+"')>forget</button>");
        document.writeln("</p>");
    }
}

function showCurrentStatus(){
    const challengerData = document.challengerData;
    const databaseData = document.databaseData;

    document.writeln("<div>");

    document.writeln(`<p><button onclick=location.reload()>Refresh Status</button></p>`);

    if(challengerData && challengerData.xChallenger){
        var xChallengerGuid = challengerData.xChallenger;

        if(challengerData.challengeStatus){
            var status = challengerData.challengeStatus;
            var doneCount = Object.values(challengerData.challengeStatus).filter(x=>x).length;
            var totalCount = Object.values(challengerData.challengeStatus).length;
            var leftCount = totalCount - doneCount;
            document.writeln(`<p>${totalCount} Challenges: ${doneCount} complete, ${leftCount} remain.`);

            document.writeln(`<button onclick="saveChallengerProgressToLocalStorage(challengerData);this.innerText='saved';this.setAttribute('disabled',true)">Save Progress to LocalStorage</button>`);
            if(localStorage.getItem(`${xChallengerGuid}.progress`)){
                document.writeln(`<button onclick="restoreChallengerProgressInSystem(challengerData)">Restore Locally Saved Progress</button>`);
            }
            document.writeln(`</p>`);

        }
        if(databaseData && databaseData.todos){

            document.writeln(`<p>${databaseData.todos.length} todos in database.</p>`);
            document.writeln(`<p>`);
            document.writeln(`<a href='/gui/instances?entity=todo'>View Todos</a> `)
            document.writeln(`<button onclick="saveChallengerTodosToLocalStorage(databaseData,challengerData);this.innerText='saved';this.setAttribute('disabled',true)">Save Todos Data to LocalStorage</button>`);
            if(localStorage.getItem(`${xChallengerGuid}.data`)){
                document.writeln(`<button onclick="restoreTodosInSystem('${xChallengerGuid}')">Restore Locally Saved Data</button>`);
            }
            document.writeln(`</p>`);
        }else{
            if(localStorage.getItem(`${xChallengerGuid}.data`)){
                document.writeln(`<button onclick="restoreTodosInSystem('${xChallengerGuid}')">Restore Locally Saved Data</button>`);
            }
        }
    }else{
        // if we have a guid in the url then allow restoring
        var parts = location.pathname.split("/");
        var possibleUuid = parts[parts.length-1];

        if(localStorage.getItem(`${possibleUuid}.progress`)){
            document.writeln(`<button onclick="restoreChallengerProgressInSystem('${possibleUuid}')">Restore Locally Saved Progress</button>`);
        }

        if(localStorage.getItem(`${possibleUuid}.data`)){
            document.writeln(`<button onclick="restoreTodosInSystem('${possibleUuid}')">Restore Locally Saved Data</button>`);
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
    .then((response) => response.json())
    .then((json) => console.log(json));

}

function restoreTodosInSystem(xchallengeruuid){

    data = localStorage.getItem(`${xchallengeruuid}.data`);
    if(data==null) return;

    fetch(`/challenger/todos/${xchallengeruuid}`, {
      method: "PUT",
      body: data,
      headers: {
        "Content-type": "application/json",
      },
    })
    .then((response) => response.json())
    .then((json) => console.log(json));

}