import type {Definition,Estimate,ServiceEstimate} from '../lib/api';
import '../styles/service-details.css';
const unknown:Estimate={displayValue:'Not enough verified information yet.',confidence:'UNKNOWN',basis:'No verified estimate is available. Check with the service or provider.',sourceUrl:null};
function normalized(estimate?:Estimate|null):Estimate {
 const supported=estimate&&typeof estimate.displayValue==='string'&&typeof estimate.basis==='string'&&estimate.basis.trim()&&(estimate.confidence!=='OFFICIAL'||estimate.sourceUrl?.startsWith('https://'));
 return supported?estimate:unknown;
}
function EstimateDetail({label,value}:{label:string;value:Estimate}) {
 const confidence=value.confidence==='OFFICIAL'?(label==='What it costs'?'Official fee':'Official estimate'):value.confidence==='PROVIDER_SPECIFIC'?'Provider-specific estimate':value.confidence==='TYPICAL'?'Typical estimate':value.confidence==='PROTOTYPE'?'Prototype estimate':'Not verified';
 return <section className={'estimate-detail '+(value.confidence==='UNKNOWN'?'unknown-estimate':'')}><h3>{label}</h3><p>{value.displayValue}</p><span className="estimate-confidence">{confidence}</span>{value.confidence!=='UNKNOWN'&&<p className="quiet estimate-basis">{value.basis}</p>}{value.sourceUrl?.startsWith('https://')&&<a className="text-link" href={value.sourceUrl} target="_blank" rel="noopener noreferrer">Estimate source</a>}</section>;
}
export default function ServiceDetails({service,definition}:{service?:ServiceEstimate|null;definition:Definition}) {
 const status=service?.status;
 const mode=status?.connectionMode==='MOCK'?'MOCK':status?.connectionMode==='LIVE'&&status.checkedAt&&Number.isFinite(Date.parse(status.checkedAt))?'LIVE':'LINK_ONLY';
 const duration=normalized(service?.duration),cost=normalized(service?.cost);
 const allUnknown=(!status||status.state==='UNKNOWN')&&duration.confidence==='UNKNOWN'&&cost.confidence==='UNKNOWN';
 const bases=[{label:'Duration',value:duration},{label:'Cost',value:cost}].filter(item=>item.value.confidence==='UNKNOWN');
 return <div className={'service-details '+(allUnknown?'all-unknown':'')}><section className="service-current"><h3>Current status</h3><p>{mode==='LINK_ONLY'?'We can’t check this status yet.':status?.plainLanguageStatus||'No current application status is recorded.'}</p><span className={'connection-mode '+mode.toLowerCase()}>{mode==='LIVE'?'Live status':mode==='MOCK'?'Prototype status':'Not connected yet'}</span>{mode==='MOCK'&&!allUnknown&&<p className="quiet">Prototype progress from your recorded answers. No official status check.</p>}{mode==='LIVE'&&<p className="quiet">Status checked {new Date(status!.checkedAt!).toLocaleString()}.</p>}</section><section><h3>What to do next</h3><ul className="detail-list">{definition.requirements.map(action=><li key={action}>{action}</li>)}</ul></section><div className="detail-facts"><EstimateDetail label="How long it usually takes" value={duration}/><EstimateDetail label="What it costs" value={cost}/></div>{bases.length>0&&<details className="estimate-bases"><summary>Estimate basis</summary>{bases.map(item=><p className="quiet" key={item.label}><strong>{item.label}: </strong>{item.value.basis}</p>)}</details>}</div>;
}
