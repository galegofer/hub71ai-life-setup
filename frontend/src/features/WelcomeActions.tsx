import {useState} from 'react';
import {ArrowRight} from 'lucide-react';
import {api,remember,type Snapshot} from '../lib/api';
export default function WelcomeActions(){
 const [busy,setBusy]=useState(false);const [error,setError]=useState('');
 async function demo(){setBusy(true);setError('');try{const s=await api<Snapshot>('/demo',{});remember(s);location.href='/plan';}catch(e){setError((e as Error).message);setBusy(false);}}
 return <><div className="welcome-actions"><a className="button primary" href="/onboarding">Get started <ArrowRight size={17}/></a><button className="button secondary" disabled={busy} onClick={demo}>{busy?'Loading…':'Try the demo'}</button></div><p className="quiet">About a minute. No documents needed.</p>{error&&<p role="alert" className="error">{error}</p>}</>;
}
