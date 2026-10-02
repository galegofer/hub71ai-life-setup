import assert from 'node:assert/strict';
import {spawn} from 'node:child_process';
import {mkdirSync,mkdtempSync,copyFileSync,writeFileSync,readFileSync} from 'node:fs';
import {resolve,join,dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
import http from 'node:http';
import {healthy,listening} from './start-windows.mjs';
const root=resolve(dirname(fileURLToPath(import.meta.url)),'..');
async function run(command,args,options={}){
 return new Promise((resolve,reject)=>{
  const child=spawn(command,args,{cwd:root,windowsHide:true,...options});let output='';
  child.stdout.on('data',data=>output+=data);child.stderr.on('data',data=>output+=data);child.on('error',reject);child.on('exit',code=>resolve({code,output}));
 });
}
assert.equal(await listening(9000),false,'Stop only the demo backend before this startup test.');
assert.equal(await listening(4321),false,'Stop only the demo frontend before this startup test.');
const fixture=mkdtempSync(join(root,'.validation-launcher-'));
mkdirSync(join(fixture,'scripts'));for(const file of ['start-windows.mjs','serve-process.mjs'])copyFileSync(join(root,'scripts',file),join(fixture,'scripts',file));
let result=await run(process.execPath,[join(fixture,'scripts','start-windows.mjs'),'--check']);
assert.equal(result.code,1);assert.match(result.output,/Missing .*life-setup-agent.jar/);
console.log('PASS: missing JAR reports the build/copy instructions.');
const system32=join(process.env.SystemRoot,'System32');
result=await run(join(system32,'cmd.exe'),['/d','/c','scripts\\Start-Windows.cmd','--check'],{env:{...process.env,PATH:system32}});
assert.equal(result.code,1);assert.match(result.output,/Install Node.js/);console.log('PASS: missing Node reports a clear error.');
result=await run(process.execPath,['scripts/start-windows.mjs','--check'],{env:{...process.env,PATH:system32}});
assert.equal(result.code,1);assert.match(result.output,/Missing java.exe/);console.log('PASS: missing Java reports a clear error.');
const server=http.createServer((_request,response)=>response.end('Unrelated service'));
await new Promise(resolve=>server.listen(4321,'127.0.0.1',resolve));
try{
 result=await run(process.execPath,['scripts/start-windows.mjs']);assert.equal(result.code,1);assert.match(result.output,/Port 4321 is occupied/);
 assert.equal(await listening(4321),true);assert.equal(await listening(9000),false);console.log('PASS: occupied frontend port is preserved; no backend was started.');
}finally{await new Promise(resolve=>server.close(resolve));}
mkdirSync(join(fixture,'backend','demo'),{recursive:true});writeFileSync(join(fixture,'backend','demo','life-setup-agent.jar'),'invalid jar for startup failure test');
mkdirSync(join(fixture,'frontend','node_modules','.bin'),{recursive:true});writeFileSync(join(fixture,'frontend','node_modules','.bin','astro.cmd'),'');
result=await run(process.execPath,[join(fixture,'scripts','start-windows.mjs')]);assert.equal(result.code,1);assert.match(result.output,/Backend exited with code 1/);
assert.match(readFileSync(join(fixture,'logs','backend.log'),'utf8'),/Invalid or corrupt jarfile/);console.log('PASS: failed backend startup reports exit code and captures the exact error.');
result=await run(join(system32,'cmd.exe'),['/d','/c','scripts\\Start-Windows.cmd']);assert.equal(result.code,0,result.output);assert.match(result.output,/Demo ready/);console.log(result.output.trim());
assert.equal(await healthy('http://127.0.0.1:9000'),true);assert.equal(await healthy('http://127.0.0.1:4321'),true);console.log('PASS: fresh Windows launcher starts both servers and API proxy.');
result=await run(join(system32,'cmd.exe'),['/d','/c','scripts\\Start-Windows.cmd']);assert.equal(result.code,0,result.output);assert.equal((result.output.match(/reusing existing process/g)||[]).length,2);console.log('PASS: repeat launcher invocation reuses both servers.');
