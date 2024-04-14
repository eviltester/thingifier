import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {

  scenarios: {
    my_scenario1: {
      executor: 'constant-arrival-rate',
      duration: '60s', // total duration
      preAllocatedVUs: 100, // to allocate runtime resources     preAll
      rate: 100, // number of constant iterations given `timeUnit`
      timeUnit: '1s',
    },
  },

//  // A number specifying the number of VUs to run concurrently.
//  vus: 100,
//  // A string specifying the total duration of the test run.
//  duration: '20s',


  // Uncomment this section to enable the use of Browser API in your tests.
  //
  // See https://grafana.com/docs/k6/latest/using-k6-browser/running-browser-tests/ to learn more
  // about using Browser API in your test scripts.
  //
  // scenarios: {
  //   // The scenario name appears in the result summary, tags, and so on.
  //   // You can give the scenario any name, as long as each name in the script is unique.
  //   ui: {
  //     // Executor is a mandatory parameter for browser-based tests.
  //     // Shared iterations in this case tells k6 to reuse VUs to execute iterations.
  //     //
  //     // See https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/ for other executor types.
  //     executor: 'shared-iterations',
  //     options: {
  //       browser: {
  //         // This is a mandatory parameter that instructs k6 to launch and
  //         // connect to a chromium-based browser, and use it to run UI-based
  //         // tests.
  //         type: 'chromium',
  //       },
  //     },
  //   },
  // }
};

// The function that defines VU logic.
//
// See https://grafana.com/docs/k6/latest/examples/get-started-with-k6/ to learn more
// about authoring k6 scripts.
//
const origin = "http://localhost:4567";

export default function() {

    const itemsJson = getItems();

    // random item id
    const randomIndex = Math.floor(Math.random()*itemsJson.items.length)
    const itemId = itemsJson.items[randomIndex].id;
    if((Math.random()*100)<5){
        // 5% chance we will get an item
        const itemJson = getItem(itemId);
    }
    if((Math.random()*100)<10){
        // 10% chance that we will delete something
        deleteItem(itemId);
    }
    if(itemsJson.items.length<80){
        createRandomItem();
    }
}

function getItems(){
  const response = http.get(`${origin}/simpleapi/items`);
  check(response, { 'get items status was 200': (r) => r.status == 200 });
  if(response.status!=200){
    console.log(response.body)
    }
  return response.json();
}

function getItem(id){
  const response = http.get(`${origin}/simpleapi/items/${id}`);
  check(response, { 'get item status was 200': (r) => r.status == 200 });
    if(response.status!=200){
      console.log(response.body)
  }
  return response.json();
}

function deleteItem(id){
  const response = http.del(`${origin}/simpleapi/items/${id}`);
  check(response, { 'delete item status was 200': (r) => r.status == 200 });
    if(response.status!=200){
      console.log(response.body)
    }
}

function createRandomItem(){
    const types = ["cd", "book", "dvd", "blu-ray"]
    const randomType = Math.floor(Math.random()*types.length)
    const isbn = ("" + Math.random()).substring(2, 8) +  ("" + Math.random()).substring(2, 8) +  ("" + Math.random()).substring(2, 3)
    const anItem =     {
                         "type": types[randomType],
                         "isbn13": isbn,
                         "price" : ((Math.random()*100+10)+"").substring(0,5)
                       }
   //console.log(anItem);
   const response = http.post(`${origin}/simpleapi/items`, JSON.stringify(anItem),
        {
            headers: { 'Content-Type': 'application/json' },
        }
   )

   if(response.status==400){
    console.log(response.body)
   }
   check(response, { 'post item status was 201': (r) => r.status == 201 });
}