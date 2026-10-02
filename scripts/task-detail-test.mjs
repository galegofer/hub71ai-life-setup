import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {writeFileSync} from 'node:fs';
const require=createRequire(import.meta.url);
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const origin=(process.env.DEMO_BASE_URL||'http://127.0.0.1:4321').replace(/\/+$/,'');
const apiOrigin=process.env.TEST_API_URL||'http://127.0.0.1:9000';
const prefix=origin.startsWith('https://')?'deployed':'windows';
const snapshot=await (await fetch(apiOrigin+'/api/demo',{method:'POST'})).json();
const browser=await chromium.launch({headless:true,channel:'msedge'});
const checks=[];
try {
 const page=await browser.newPage({viewport:{width:390,height:844},reducedMotion:'reduce'});
 const errors=[];page.on('pageerror',error=>errors.push(error.message));
 await page.addInitScript(id=>localStorage.setItem('life-setup-profile',id),snapshot.profileId);
 await page.goto(origin+'/plan');await page.getByRole('heading',{name:'Your Abu Dhabi setup'}).waitFor();
 const drawer=page.locator('.drawer');
 for(const task of snapshot.plan.tasks) {
  await page.locator('.task-open').filter({has:page.getByText(task.definition.title,{exact:true})}).click();
  for(const title of ['Current status','What to do next','How long it usually takes','What it costs'])await drawer.getByRole('heading',{name:title,exact:true}).waitFor();
  const mode=['residence','emirates-id'].includes(task.definition.id)?'Prototype status':'Not connected yet';await drawer.getByText(mode,{exact:true}).waitFor();
  assert.equal(await drawer.getByText('Live status',{exact:true}).count(),0);assert.equal(await drawer.getByText('Official fee',{exact:true}).count(),0);
  assert.equal(await drawer.getByText('Not verified',{exact:true}).count(),2);
  if(task.definition.officialSource)assert.equal(await drawer.locator('.source-box a').getAttribute('href'),task.definition.officialSource.url);
  else await drawer.getByText(/No official service link is recorded/).waitFor();
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
  assert.equal(await drawer.evaluate(node=>node.scrollWidth<=node.clientWidth),true);
  if(task.definition.id==='emirates-id') {await drawer.getByText('Your application is being processed.',{exact:true}).waitFor();await page.screenshot({path:'docs/'+prefix+'-id-detail-mobile.png'});}
  await page.keyboard.press('Escape');
 }
 checks.push('All eleven mobile drawers show mode, next action, unknown estimates, basis and existing source/provider guidance without overflow');
 const id=snapshot.plan.tasks.find(task=>task.definition.id==='emirates-id');
 async function fixture(service) {
  await page.unroute('**/api/plan/**');
  const data=structuredClone(snapshot);data.plan.tasks.find(task=>task.definition.id==='emirates-id').serviceEstimate=service;
  await page.route('**/api/plan/**',route=>route.fulfill({status:200,contentType:'application/json',body:JSON.stringify(data)}));
  await page.reload();await page.locator('.task-open').filter({has:page.getByText(id.definition.title,{exact:true})}).click();
 }
 await fixture({status:{connectionMode:'LIVE',state:'IN_PROGRESS',plainLanguageStatus:'Connected status test fixture.',checkedAt:'2026-10-02T10:00:00Z'},duration:null,cost:{displayValue:'Source-backed test fixture',confidence:'OFFICIAL',basis:'Rendering fixture only; no government fee claim.',sourceUrl:id.definition.officialSource.url},officialActionUrl:id.definition.officialSource.url});
 await drawer.getByText('Live status',{exact:true}).waitFor();await drawer.getByText('Official fee',{exact:true}).waitFor();await drawer.getByRole('link',{name:'Estimate source'}).waitFor();assert.equal(await drawer.getByText('Prototype status',{exact:true}).count(),0);await drawer.getByText('Not enough verified information yet.',{exact:true}).waitFor();
 checks.push('LIVE mode and source-backed OFFICIAL rendering work using clearly isolated fixtures; missing duration remains unknown');
 await fixture({status:{connectionMode:'LIVE',state:'UNKNOWN',plainLanguageStatus:'Invalid unchecked fixture',checkedAt:null},duration:null,cost:{displayValue:'Unsupported official fixture',confidence:'OFFICIAL',basis:'No source',sourceUrl:null},officialActionUrl:null});
 await drawer.getByText('Not connected yet',{exact:true}).waitFor();assert.equal(await drawer.getByText('Official fee',{exact:true}).count(),0);assert.equal(await drawer.getByText('Unsupported official fixture',{exact:true}).count(),0);
 await fixture(null);await drawer.getByText('Not connected yet',{exact:true}).waitFor();assert.equal(await drawer.getByText('Not enough verified information yet.',{exact:true}).count(),2);
 checks.push('Missing service data, missing estimates and unsupported official attribution safely display unknown values');
 assert.deepEqual(errors,[]);
 writeFileSync('docs/'+prefix+'-task-detail-validation.json',JSON.stringify({date:new Date().toISOString(),origin,checks},null,2)+'\n');
 for(const check of checks)console.log('PASS: '+check);
}finally{await browser.close();}
