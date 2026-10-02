// Keep a hidden supervisor alive so Windows gives Java valid log handles.
// Java suppresses stdout/stderr when spawned directly with DETACHED_PROCESS.
import {spawn} from 'node:child_process';
import {openSync,closeSync,appendFileSync} from 'node:fs';
const [log,command,...args]=process.argv.slice(2);
const fd=openSync(log,'a');
const child=spawn(command,args,{windowsHide:true,stdio:['ignore',fd,fd]});
closeSync(fd);
child.once('error',error=>{appendFileSync(log,`Cannot start ${command}: ${error.message}\n`);process.exitCode=1;});
child.once('exit',(code)=>{process.exitCode=code??1;});
