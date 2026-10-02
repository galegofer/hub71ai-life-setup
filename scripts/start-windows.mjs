import {spawn, spawnSync} from 'node:child_process';
import {existsSync, mkdirSync} from 'node:fs';
import {dirname, join, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import net from 'node:net';
import {setTimeout as delay} from 'node:timers/promises';

const root=resolve(dirname(fileURLToPath(import.meta.url)), '..');
const backend='http://127.0.0.1:9000';
const frontend='http://127.0.0.1:4321';

function tool(name){
 const result=spawnSync('where.exe',[name],{encoding:'utf8',windowsHide:true});
 if(result.status!==0)throw new Error(`Missing ${name}. Install it and reopen your terminal.`);
 return result.stdout.trim().split(/\r?\n/)[0];
}
export function listening(port){
 return new Promise(resolve=>{
  const socket=net.createConnection({host:'127.0.0.1',port});
  const finish=value=>{socket.destroy();resolve(value);};
  socket.setTimeout(1000);socket.once('connect',()=>finish(true));socket.once('error',()=>finish(false));socket.once('timeout',()=>finish(false));
 });
}
export async function healthy(origin,deadline=Date.now()+1500){
 try{const response=await fetch(origin+'/api/health',{signal:AbortSignal.timeout(Math.max(1,Math.min(1500,deadline-Date.now())))});if(!response.ok)return false;const body=await response.json();return body.status==='ok'&&body.mode==='prototype';}catch{return false;}
}
export async function isFrontend(origin,deadline=Date.now()+1500){
 try{const response=await fetch(origin,{signal:AbortSignal.timeout(Math.max(1,Math.min(1500,deadline-Date.now())))});return response.ok&&(await response.text()).includes('<meta name="life-setup-app" content="life-setup-agent"');}catch{return false;}
}
export async function waitReady(name,check,child,log,timeout=30000){
 const deadline=Date.now()+timeout;
 do{
  if(child?.exitCode!==null&&child?.exitCode!==undefined)throw new Error(`${name} exited with code ${child.exitCode}. Read ${log}`);
  if(await check(deadline))return;
  await delay(Math.min(400,Math.max(0,deadline-Date.now())));
 }while(Date.now()<deadline);
 throw new Error(`${name} was not ready within ${timeout/1000} seconds. Read ${log}`);
}
function background(command,args,cwd,env,log){
 const child=spawn(process.execPath,[join(root,'scripts','serve-process.mjs'),log,command,...args],{cwd,env,detached:true,windowsHide:true,stdio:'ignore'});
 return new Promise((resolve,reject)=>{child.once('error',e=>reject(new Error(`Cannot start ${command}: ${e.message}. Read ${log}`)));child.once('spawn',()=>resolve(child));});
}
export async function main(){
 if(process.argv.slice(2).some(arg=>arg!=='--check'))throw new Error('Usage: Start-Windows.cmd [--check]');
 const [major,minor]=process.versions.node.split('.').map(Number);
 if(major<22||(major===22&&minor<12))throw new Error('Use Node.js 22.12 or newer. Node 24 is recommended.');
 const java=tool('java.exe');const npm=tool('npm.cmd');
 const version=spawnSync(java,['-version'],{encoding:'utf8',windowsHide:true});
 if(version.status!==0||!/(?:java|openjdk) version "25(?:\.|"|\-)/.test(version.stderr+version.stdout))throw new Error('Use Java 25. Check java -version and your PATH.');
 const npmCli=join(dirname(npm),'node_modules','npm','bin','npm-cli.js');
 if(!existsSync(npmCli))throw new Error(`Cannot find npm beside ${npm}. Repair the Node/npm installation.`);
 const jar=join(root,'backend','demo','life-setup-agent.jar');
 if(!existsSync(jar))throw new Error(`Missing ${jar}. Run mvn package in backend and copy the packaged JAR into backend/demo.`);
 const [backendUp,frontendUp]=await Promise.all([listening(9000),listening(4321)]);
 if(backendUp&&!await healthy(backend))throw new Error('Port 9000 is occupied by an unrecognized or unhealthy service. Close the conflicting service yourself; no process was stopped.');
 if(frontendUp&&!await isFrontend(frontend))throw new Error('Port 4321 is occupied by an unrecognized or unhealthy service. Close the conflicting service yourself; no process was stopped.');
 if(process.argv.includes('--check')){console.log(`Prerequisites OK. Backend: ${backendUp?'running':'not running'}. Frontend: ${frontendUp?'running':'not running'}.`);return;}
 const logs=join(root,'logs');mkdirSync(logs,{recursive:true});
 const env={...process.env,BACKEND_URL:backend};
 if(!frontendUp&&!existsSync(join(root,'frontend','node_modules','.bin','astro.cmd'))){
  console.log('Installing frontend dependencies…');
  const install=spawnSync(process.execPath,[npmCli,'ci'],{cwd:join(root,'frontend'),env,stdio:'inherit',windowsHide:true});
  if(install.status!==0)throw new Error('Frontend dependency installation failed. Read the npm error above.');
 }
 const started=[];
 try{
  let child;
  if(!backendUp){child=await background(java,['-jar',jar,'--server.port=9000','--server.address=127.0.0.1'],join(root,'backend'),env,join(logs,'backend.log'));started.push(child);}
  await waitReady('Backend',deadline=>healthy(backend,deadline),child,join(logs,'backend.log'));
  console.log(`Backend ready: ${backend}${backendUp?' (reusing existing process)':''}`);
  child=undefined;
  if(!frontendUp){child=await background(process.execPath,[npmCli,'run','dev','--','--port','4321'],join(root,'frontend'),env,join(logs,'frontend.log'));started.push(child);}
  await waitReady('Frontend',async deadline=>await isFrontend(frontend,deadline)&&Date.now()<deadline&&await healthy(frontend,deadline),child,join(logs,'frontend.log'));
  console.log(`Demo ready: ${frontend}${frontendUp?' (reusing existing process)':''}\nLogs: ${logs}\nKeep both servers running during the demo.`);
 }finally{for(const child of started)child.unref();}
}
if(process.argv[1]&&resolve(process.argv[1])===fileURLToPath(import.meta.url))main().catch(error=>{console.error(`ERROR: ${error.message}`);process.exitCode=1;});
