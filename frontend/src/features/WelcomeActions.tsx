import {useState} from 'react';
import {ArrowRight} from 'lucide-react';
export default function WelcomeActions(){
 const [busy,setBusy]=useState(false);
 function demo(){setBusy(true);location.href='/describe?demo';}
 return <><div className="welcome-actions"><a className="button primary" href="/describe">Describe your move <ArrowRight size={17}/></a><button className="button secondary" disabled={busy} onClick={demo}>{busy?'Loading…':'Try the demo'}</button><a className="text-link" href="/onboarding">Get started</a></div><p className="quiet">About a minute. No documents needed.</p></>;
}
