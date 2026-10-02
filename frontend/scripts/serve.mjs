import {spawn} from 'node:child_process';
const port=Number(process.env.PORT || 4321);
if(!Number.isInteger(port)||port<1||port>65535)throw new Error('PORT must be a valid port number.');
const child=spawn(process.execPath,['node_modules/serve/build/main.js','dist','--listen',`tcp://0.0.0.0:${port}`,'--no-clipboard'],{stdio:'inherit'});
child.on('error',error=>{console.error(error.message);process.exitCode=1;});
child.on('exit',code=>{process.exitCode=code ?? 1;});
for(const signal of ['SIGINT','SIGTERM'])process.on(signal,()=>child.kill(signal));
