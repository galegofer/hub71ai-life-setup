import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import http from 'node:http';
import ts from '../frontend/node_modules/typescript/lib/typescript.js';
import {healthy,isFrontend,listening,waitReady} from './start-windows.mjs';
const source=readFileSync(new URL('../frontend/src/lib/api.ts',import.meta.url),'utf8');
const js=ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.ESNext,target:ts.ScriptTarget.ES2022}}).outputText;
const {api,ApiError,expiredSession,retryable}=await import('data:text/javascript;base64,'+Buffer.from(js).toString('base64'));

test('API distinguishes expiry, server outages and malformed replies',async()=>{
 const fetch=globalThis.fetch;
 try{
  globalThis.fetch=async()=>new Response(JSON.stringify({message:'Session expired.'}),{status:404});
  await assert.rejects(api('/plan/missing'),error=>error instanceof ApiError&&expiredSession(error)&&!retryable(error));
  globalThis.fetch=async()=>new Response('Proxy failed',{status:503});
  await assert.rejects(api('/plan/test'),error=>error.status===503&&retryable(error)&&!expiredSession(error));
  for(const reply of ['not JSON','null','{}']){
   globalThis.fetch=async()=>new Response(reply);
   await assert.rejects(api('/plan/test'),error=>error.kind==='malformed'&&retryable(error));
  }
  globalThis.fetch=async()=>{throw new TypeError('Failed to fetch');};
  await assert.rejects(api('/plan/test'),error=>error.kind==='network'&&error.status===null&&retryable(error));
 }finally{globalThis.fetch=fetch;}
});
test('API bounds requests and classifies timeouts',async()=>{
 const fetch=globalThis.fetch;const timer=globalThis.setTimeout;
 try{
  globalThis.setTimeout=(callback,ms)=>{assert.equal(ms,10000);return timer(callback,1);};
  globalThis.fetch=async(_url,{signal})=>new Promise((_resolve,reject)=>signal.addEventListener('abort',()=>reject(new Error('aborted'))));
  await assert.rejects(api('/plan/test'),error=>error.kind==='timeout'&&error.status===null);
 }finally{globalThis.fetch=fetch;globalThis.setTimeout=timer;}
});
test('launcher rejects unrelated listeners and reports exit/timeout failures',async()=>{
 const server=http.createServer((_request,response)=>response.end('Unrelated service'));
 await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
 const port=server.address().port;const origin=`http://127.0.0.1:${port}`;
 try{
  assert.equal(await listening(port),true);assert.equal(await healthy(origin),false);assert.equal(await isFrontend(origin),false);
  await assert.rejects(waitReady('Backend',()=>false,{exitCode:1},'backend.log',100),/exited with code 1.*backend.log/);
  await assert.rejects(waitReady('Frontend',()=>false,undefined,'frontend.log',20),/not ready.*frontend.log/);
 }finally{await new Promise(resolve=>server.close(resolve));}
});
