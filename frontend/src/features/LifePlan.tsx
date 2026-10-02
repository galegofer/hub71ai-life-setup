import {useEffect,useRef,useState} from 'react';

import {ArrowRight,Check,CheckCircle2,ChevronRight,Clock,ExternalLink,LockKeyhole,MessageCircle,RotateCcw,X} from 'lucide-react';

import {motion,useReducedMotion} from 'motion/react';

import {api,remember,expiredSession,forgetSession,retryable,type Snapshot,type Task,type Source} from '../lib/api';

import {countryName,useCountries} from '../lib/countries';

import ServiceDetails from './ServiceDetails';

const labels={READY:'You can do this now',IN_PROGRESS:'In progress',BLOCKED:'Waiting for another step',DONE:'Done',LATER:'Saved for later'};

export default function LifePlan(){

 const {countries}=useCountries();

 const [snapshot,setSnapshot]=useState<Snapshot|null>(null);const [error,setError]=useState('');const [loading,setLoading]=useState(true);const [busy,setBusy]=useState(false);const [selected,setSelected]=useState<string|null>(null);const [chat,setChat]=useState(false);const [notice,setNotice]=useState('');const [question,setQuestion]=useState('');const [answer,setAnswer]=useState('');const [sources,setSources]=useState<Source[]>([]);const [asking,setAsking]=useState(false);

 const [retry,setRetry]=useState<(()=>void)|null>(null);const [expired,setExpired]=useState(false);

 const dialog=useRef<HTMLDialogElement>(null);const trigger=useRef<HTMLElement|null>(null);const nextButton=useRef<HTMLButtonElement>(null);const nextHeading=useRef<HTMLHeadingElement>(null);const focusNext=useRef(false);const revision=useRef(0);const reduce=useReducedMotion();

 function clearError(){setError('');setRetry(null);}

 function failed(cause:unknown,again:()=>void){

  if(expiredSession(cause)){forgetSession();revision.current++;setSnapshot(null);setSelected(null);setChat(false);setAnswer('');setSources([]);setExpired(true);setNotice('');setRetry(null);setError('Your prototype session has expired. Start a new plan or load the demo.');}

  else {setError((cause as Error).message);setRetry(retryable(cause)?()=>again:null);}

 }

 async function load(){const id=localStorage.getItem('life-setup-profile');if(!id){setLoading(false);return;}setLoading(true);clearError();try{setSnapshot(await api<Snapshot>('/plan/'+id));}catch(e){failed(e,load);}finally{setLoading(false);}}

 useEffect(()=>{void load();},[]);

 useEffect(()=>{const d=dialog.current;if(!d)return;if(selected||chat){if(!d.open)d.showModal();}else {if(d.open){d.close();if(!focusNext.current&&trigger.current?.isConnected)trigger.current.focus();}if(focusNext.current){focusNext.current=false;(nextButton.current||nextHeading.current)?.focus();}}},[selected,chat,snapshot]);

 function openPanel(id:string|null){trigger.current=document.activeElement as HTMLElement;setSelected(id);setChat(id===null);clearError();}

 async function demo(){setBusy(true);clearError();try{const s=await api<Snapshot>('/demo',{});remember(s);revision.current++;setSnapshot(s);setExpired(false);setSelected(null);setChat(false);setAnswer('');setSources([]);setQuestion('');setNotice('Demo reset. Meet your next steps.');}catch(e){failed(e,demo);}finally{setBusy(false);}}

 async function action(id:string,kind:string){if(!snapshot)return;setBusy(true);clearError();try{const s=await api<Snapshot>('/plan/'+snapshot.profileId+'/tasks/'+id,{action:kind});revision.current++;setSnapshot(s);setAnswer('');setSources([]);if(kind==='complete'){focusNext.current=true;setSelected(null);setChat(false);}const unlocked=s.newlyReadyTaskIds.map(id=>s.plan.tasks.find(t=>t.definition.id===id)?.definition.title).filter(Boolean);setNotice(unlocked.length?'Good news. '+(unlocked.length===2?'Two':unlocked.length)+' '+(unlocked.length===1?'step is':'steps are')+' now ready. '+unlocked.join(' · '):kind==='undo'?'Your plan has been updated.':'Progress saved. One step closer.');}catch(e){failed(e,()=>void action(id,kind));}finally{setBusy(false);}}

 async function askQuestion(){if(!snapshot||!question.trim())return;setAsking(true);clearError();const version=revision.current;try{const r=await api<{answer:string;sources:Source[]}>('/assistant',{profileId:snapshot.profileId,question});if(version===revision.current){setAnswer(r.answer);setSources(r.sources);}}catch(e){if(version===revision.current)failed(e,askQuestion);}finally{setAsking(false);}}

 function ask(e:React.SyntheticEvent<HTMLFormElement>){e.preventDefault();void askQuestion();}

 function close(){setSelected(null);setChat(false);clearError();}

 function errorMessage(){return error&&<div role="alert" className="error"><p>{error}</p>{retry&&<button className="text-link" disabled={busy||asking||loading} onClick={retry}>Try again</button>}</div>;}

 function title(id:string){return snapshot?.plan.tasks.find(t=>t.definition.id===id)?.definition.title || id;}

 function card(t:Task){const d=t.definition;const isNew=snapshot?.newlyReadyTaskIds.includes(d.id);return <motion.article layout={!reduce} initial={false} animate={{opacity:1}} transition={{duration:reduce?0:0.22}} className={'task-card '+(isNew?'newly-ready ':'')+(t.status==='DONE'?'completed-card':'')+(d.id===snapshot?.plan.nextBestTaskId?' recommended-task':'')} key={d.id}>

  <button className="task-open" onClick={()=>openPanel(d.id)}><span className={'task-symbol '+t.status.toLowerCase()}>{t.status==='DONE'?<Check size={17}/>:t.status==='BLOCKED'?<LockKeyhole size={15}/>:t.status==='IN_PROGRESS'?<Clock size={17}/>:<span/>}</span><span className="task-copy"><span className="task-title">{d.title}</span><span className="task-summary">{t.status==='BLOCKED'?'First, '+t.waitingFor.map(title).join(' and ')+'.':d.summary}</span></span><ChevronRight size={18} className="card-chevron"/></button>

  {t.immediateUnlockTaskIds&&t.immediateUnlockTaskIds.length>0&&<p className="unlock-hint">Unlocks {t.immediateUnlockTaskIds.length} next {t.immediateUnlockTaskIds.length===1?'step':'steps'}</p>}<div className="task-bottom"><span className={'status '+t.status.toLowerCase()}>{labels[t.status]}</span>{(t.status==='READY'||t.status==='IN_PROGRESS')&&<button disabled={busy} className="small-action" onClick={()=>action(d.id,'complete')}>{d.completionLabel||'Mark this step done'} <Check size={13}/></button>}</div></motion.article>;}

 const tasks=snapshot?.plan.tasks || [];const current=tasks.find(t=>t.definition.id===selected);const recommended=tasks.find(t=>t.definition.id===snapshot?.plan.nextBestTaskId);const blocker=tasks.find(t=>t.definition.id===snapshot?.plan.biggestBlockerTaskId);

 if(loading)return <section className="plan-shell"><p className="muted" role="status">Loading your next steps…</p></section>;

 if(!snapshot)return <section className="empty-state"><h1>{expired?'Let’s make a new plan.':'Let’s make your plan.'}</h1>{errorMessage()}{!error&&<p>Tell us about your move or try the demo profile.</p>}<div className="welcome-actions"><a className="button primary" href="/onboarding">Get started</a><button className="button secondary" disabled={busy} onClick={demo}>{busy?'Loading…':'Load demo profile'}</button></div></section>;

 return <section className="plan-shell"><div className="plan-heading"><div><div className="eyebrow">Your move, made simpler</div><h1>Your Abu Dhabi setup</h1><p className="plan-subtitle">You have <strong>{snapshot.plan.remaining} things left to do.</strong> You can work on <strong>{snapshot.plan.ready} now.</strong> {snapshot.plan.waiting??tasks.filter(t=>t.status==='BLOCKED').length} waiting. {snapshot.plan.later??tasks.filter(t=>t.status==='LATER').length} saved for later.</p></div><button className="button secondary assistant-trigger" onClick={()=>openPanel(null)}><MessageCircle size={17}/>Ask Life Setup</button></div>

 <div className="profile-strip"><span>{countryName(snapshot.profile.nationality,countries)} · {snapshot.profile.alreadyInUae?'Already in the UAE':'Preparing to move'}{snapshot.profile.movingWithFamily?' · Moving with family':''}{snapshot.profile.bringingPet?' · Bringing a pet':''}</span><a href="/onboarding?edit">Edit your answers <ChevronRight size={14}/></a></div>

 {snapshot.plan.situationSummary&&<section className="situation-summary"><div><h2>Your situation</h2><ul>{snapshot.plan.situationSummary.map(fact=><li key={fact}>{fact}</li>)}</ul></div>{blocker&&<div><h2>Your biggest blocker</h2><strong>{blocker.definition.title}</strong>{blocker.status==='LATER'&&<p>Resume this step when you're ready.</p>}{blocker.status==='BLOCKED'&&<p>{blocker.nextAction}</p>}<p>This unlocks:</p><ul>{blocker.immediateUnlockTaskIds?.map(id=><li key={id}>{title(id)}</li>)}</ul></div>}</section>}<div className="progress-row"><div className="thin-progress"><div style={{width:(snapshot.plan.completed/tasks.length*100)+'%'}}/></div><span>{snapshot.plan.completed} of {tasks.length} done</span></div>

 <div className="notice" role="status" aria-live="polite">{notice&&<><CheckCircle2 size={18}/><span>{notice}</span></>}</div>{!(selected||chat)&&errorMessage()}

 <div className="plan-layout"><div className="next-step aside-card"><span className="eyebrow">Do this next</span><h2 ref={nextHeading} tabIndex={-1}>{snapshot.plan.nextBestAction}</h2>{snapshot.plan.nextBestTaskId&&<><p>{recommended?.planStatusSummary||recommended?.definition.summary}</p><p><strong>What to do now: </strong>{recommended?.nextAction||recommended?.definition.requirements.join(' ')}</p>{snapshot.plan.nextBestUnlockTaskIds?.length>0&&<div className="unlock-preview"><p>This unlocks:</p><ul>{snapshot.plan.nextBestUnlockTaskIds.map(id=><li key={id}>{title(id)}</li>)}</ul></div>}<button ref={nextButton} className="text-link" onClick={()=>openPanel(snapshot.plan.nextBestTaskId)}>View step <ArrowRight size={16}/></button></>}</div><div className="task-sections">{[{name:'Do this now',description:'These steps are ready for you.',states:['READY','IN_PROGRESS']},{name:'Coming next',description:'These steps are waiting for something else.',states:['BLOCKED']},{name:'Later',description:'Steps you’ve saved for another time.',states:['LATER']}].map(group=>{const items=tasks.filter(t=>group.states.includes(t.status));return items.length>0&&<section className="task-section" key={group.name}><div className="section-heading"><h2>{group.name}<span>{items.length}</span></h2><p>{group.description}</p></div><div className="task-grid">{items.map(card)}</div></section>;})}

 {tasks.some(t=>t.status==='DONE')&&<details className="done-section"><summary><CheckCircle2 size={17}/>Completed steps <span>{snapshot.plan.completed}</span></summary><div className="task-grid">{tasks.filter(t=>t.status==='DONE').map(card)}</div></details>}

 {snapshot.plan.remaining===0&&<div className="all-done"><CheckCircle2 size={28}/><h2>You’re all set for now.</h2><p>Your completed steps are saved below.</p></div>}</div>

 <aside className="plan-aside"><div className="aside-note"><h3>Your plan moves with you.</h3><p>Mark a step done and we’ll show you what becomes ready next.</p><p className="quiet">Prototype rules guide the order. Official services confirm what applies to you.</p></div><button className="reset-button" disabled={busy} onClick={demo}><RotateCcw size={14}/>Reset demo profile</button></aside></div>

 <dialog ref={dialog} className="drawer" onCancel={close} onClick={e=>{if(e.target===dialog.current){const r=dialog.current.getBoundingClientRect();if(e.clientX<r.left||e.clientX>r.right||e.clientY<r.top||e.clientY>r.bottom)close();}}} aria-labelledby="drawer-title"><div className="drawer-inner"><button autoFocus className="close-button" onClick={close} aria-label="Close panel"><X size={20}/></button>

 {current&&<><div className="eyebrow">Your next steps</div><h2 id="drawer-title">{current.definition.title}</h2><div className="plan-state"><span className="quiet">Your plan</span><span className={'status '+current.status.toLowerCase()}>{labels[current.status]}</span></div><p className="detail-intro">{current.definition.summary}</p>{current.status==='BLOCKED'&&<div className="blocking-box"><LockKeyhole size={18}/><div><strong>You can’t do this yet.</strong><p>First, {current.waitingFor.map(id=>title(id).toLowerCase()).join(' and ')}.</p></div></div>}

 <ServiceDetails service={current.serviceEstimate} definition={current.definition} task={current} title={title}/><p className="prototype-note">{current.definition.note}</p>

 {current.definition.officialSource?<div className="source-box"><span className="eyebrow">Official information</span><h3>{current.definition.officialSource.authority}</h3><p>{current.definition.officialSource.title}</p><p className="quiet">Link discovered {current.definition.officialSource.lastChecked}. {current.definition.officialSource.scope}</p><a className="button secondary" href={current.definition.officialSource.url} target="_blank" rel="noopener noreferrer">{current.definition.nextAction}<ExternalLink size={15}/></a><span className="quiet">Opens an official website.</span></div>:<div className="source-box"><span className="eyebrow">Provider information</span><p>No official service link is recorded for this step. Confirm details with your chosen provider.</p></div>}

 <div className="detail-actions">{(current.status==='READY'||current.status==='IN_PROGRESS')&&<><button className="button primary" disabled={busy} onClick={()=>action(current.definition.id,'complete')}>{busy?'Saving…':current.definition.completionLabel||'Mark this step done'}<Check size={16}/></button><button className="button text" disabled={busy} onClick={()=>action(current.definition.id,'defer')}>Save for later</button></>}{current.status==='DONE'&&<button className="button secondary" disabled={busy} onClick={()=>action(current.definition.id,'undo')}>Undo completion</button>}{current.status==='LATER'&&<button className="button primary" disabled={busy} onClick={()=>action(current.definition.id,'resume')}>Move back to my plan</button>}</div></>}

 {chat&&<><div className="eyebrow">A little help with your plan</div><h2 id="drawer-title">Ask Life Setup</h2><p className="muted">Ask about your next step. Answers use your current plan.</p><button className="suggested-question" onClick={()=>setQuestion('Can I exchange my driving licence now?')}>Can I exchange my driving licence now?</button><form onSubmit={ask}><label>Your question<textarea maxLength={500} value={question} onChange={e=>setQuestion(e.target.value)} placeholder="What can I do next?" rows={3}/></label><button className="button primary" disabled={asking||!question.trim()}>{asking?'Checking your plan…':'Ask'}<ArrowRight size={16}/></button></form>{answer&&<div className="answer" aria-live="polite"><p>{answer}</p>{sources.map(s=><a className="text-link" key={s.url} href={s.url} target="_blank" rel="noopener noreferrer">{s.authority}: official information <ExternalLink size={14}/></a>)}</div>}<p className="quiet assistant-note">This prototype explains your plan. Official services confirm eligibility, documents and costs.</p></>}

 {errorMessage()}

 </div></dialog></section>;

}

