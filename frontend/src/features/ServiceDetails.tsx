import type {Definition,Estimate,ServiceEstimate} from '../lib/api';
import '../styles/service-details.css';
const unknown:Estimate={displayValue:'Not enough verified information yet.',confidence:'UNKNOWN',basis:'No verified estimate is available. Check with the service or provider.',sourceUrl:null};
function EstimateDetail({label,estimate}:{label:string;estimate?:Estimate|null}) {
 const supported=estimate&&typeof estimate.displayValue==='string'&&typeof estimate.basis==='string'&&estimate.basis.trim()&&(estimate.confidence!=='OFFICIAL'||estimate.sourceUrl?.startsWith('https://'));
 const value=supported?estimate:unknown;
 const confidence=value.confidence==='OFFICIAL'?(label==='What it costs'?'Official fee':'Official estimate'):value.confidence==='PROVIDER_SPECIFIC'?'Provider-specific estimate':value.confidence==='TYPICAL'?'Typical estimate':value.confidence==='PROTOTYPE'?'Prototype estimate':'Not verified';
 return <section className="estimate-detail"><h3>{label}</h3><p>{value.displayValue}</p><span className="estimate-confidence">{confidence}</span><p className="quiet estimate-basis">{value.basis}</p>{value.sourceUrl?.startsWith('https://')&&<a className="text-link" href={value.sourceUrl} target="_blank" rel="noopener noreferrer">Estimate source</a>}</section>;
}
export default function ServiceDetails({service,definition}:{service?:ServiceEstimate|null;definition:Definition}) {
 const status=service?.status;
 const mode=status?.connectionMode==='MOCK'?'MOCK':status?.connectionMode==='LIVE'&&status.checkedAt&&Number.isFinite(Date.parse(status.checkedAt))?'LIVE':'LINK_ONLY';
 return <div className="service-details"><section className="service-current"><h3>Current status</h3><p>{mode==='LINK_ONLY'?'We can’t check this status yet.':status?.plainLanguageStatus||'No current application status is recorded.'}</p><span className={'connection-mode '+mode.toLowerCase()}>{mode==='LIVE'?'Live status':mode==='MOCK'?'Prototype status':'Not connected yet'}</span>{mode==='MOCK'&&<p className="quiet">Based on recorded answers and prototype progress. No official status has been checked.</p>}{mode==='LINK_ONLY'&&<p className="quiet">{definition.officialSource?'You can check it on the official service.':'Check directly with your chosen provider.'}</p>}{mode==='LIVE'&&<p className="quiet">Status checked {new Date(status!.checkedAt!).toLocaleString()}.</p>}</section><section><h3>What to do next</h3><ul className="detail-list">{definition.requirements.map(action=><li key={action}>{action}</li>)}</ul></section><div className="detail-facts"><EstimateDetail label="How long it usually takes" estimate={service?.duration}/><EstimateDetail label="What it costs" estimate={service?.cost}/></div></div>;
}
