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