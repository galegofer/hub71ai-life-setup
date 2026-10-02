export type Status='READY'|'IN_PROGRESS'|'BLOCKED'|'DONE'|'LATER';
export interface Profile { nationality:string|null;movingFrom:string|null;alreadyInUae:boolean;movingWithFamily:boolean;movingWithChildren:boolean;residenceStatus:'NOT_STARTED'|'IN_PROGRESS'|'COMPLETE';hasEmiratesId:boolean;hasHousing:boolean;wantsToDrive:boolean;licenceCountry:string|null;bringingPet:boolean }
export interface Source { authority:string;title:string;url:string;lastChecked:string;scope:string }
export interface Definition { id:string;title:string;summary:string;dependencies:string[];requirements:string[];officialSource:Source|null;nextAction:string;note:string;category:string;applicability:string;priority:number;completionLabel:string;reviewedDuration:Estimate|null;reviewedCost:Estimate|null }
export type ConnectionMode='LIVE'|'MOCK'|'LINK_ONLY';
export type EstimateConfidence='OFFICIAL'|'PROVIDER_SPECIFIC'|'TYPICAL'|'PROTOTYPE'|'UNKNOWN';
export interface Estimate { displayValue:string;confidence:EstimateConfidence;basis:string;sourceUrl:string|null }
export interface ServiceEstimate { status:{connectionMode:ConnectionMode;state:'UNKNOWN'|'NOT_STARTED'|'IN_PROGRESS'|'COMPLETED';plainLanguageStatus:string;checkedAt:string|null};duration:Estimate|null;cost:Estimate|null;officialActionUrl:string|null }
export interface Task { definition:Definition;status:Status;waitingFor:string[];serviceEstimate?:ServiceEstimate|null }
export interface Snapshot { profileId:string;profile:Profile;plan:{tasks:Task[];remaining:number;ready:number;completed:number;nextBestAction:string;nextBestTaskId:string|null;nextBestUnlockTaskIds:string[]};newlyReadyTaskIds:string[] }
export class ApiError extends Error {
 constructor(message:string,public status:number|null,public kind:'http'|'network'|'timeout'|'malformed',public sessionExpired=false) { super(message);this.name='ApiError'; }
}
export function expiredSession(error:unknown):boolean { return error instanceof ApiError && error.status===404 && error.sessionExpired; }
export function retryable(error:unknown):boolean { return error instanceof ApiError && (error.kind!=='http' || (error.status!==null && error.status>=500)); }
export function forgetSession() { localStorage.removeItem('life-setup-profile'); }
function validReply(path:string,data:Record<string,unknown>):boolean {
 if(path==='/profile/extract') {
  const draft=data.draft as Record<string,unknown>|undefined;
  return !!draft && (data.mode==='OPENAI'||data.mode==='FALLBACK')
   && ['nationality','movingFrom','licenceCountry'].every(key=>draft[key]===null||typeof draft[key]==='string')
   && ['alreadyInUae','movingWithFamily','movingWithChildren','hasEmiratesId','hasHousing','wantsToDrive','bringingPet'].every(key=>draft[key]===null||typeof draft[key]==='boolean')
   && (draft.residenceStatus===null||['NOT_STARTED','IN_PROGRESS','COMPLETE'].includes(String(draft.residenceStatus)));
 }
 if(/^\/(demo|profile|plan)(\/|$)/.test(path)) {
  const plan=data.plan as Snapshot['plan']|undefined;
  return typeof data.profileId==='string' && typeof data.profile==='object' && data.profile!==null && !!plan && Array.isArray(plan.tasks) && Array.isArray(data.newlyReadyTaskIds)
   && typeof plan.nextBestAction==='string' && (plan.nextBestTaskId===null || typeof plan.nextBestTaskId==='string')
   && Array.isArray(plan.nextBestUnlockTaskIds) && plan.nextBestUnlockTaskIds.every(id=>typeof id==='string')
   && [plan.remaining,plan.ready,plan.completed].every(value=>typeof value==='number'&&Number.isFinite(value))
   && plan.tasks.every(task=>task&&task.definition&&typeof task.definition.id==='string'&&typeof task.definition.title==='string'&&typeof task.definition.summary==='string'&&Array.isArray(task.definition.requirements)&&Array.isArray(task.waitingFor)&&['READY','IN_PROGRESS','BLOCKED','DONE','LATER'].includes(task.status));
 }
 if(path==='/assistant')return typeof data.answer==='string'&&Array.isArray(data.sources);
 return true;
}
export function apiUrl(path:string):string {
 const base=(import.meta.env?.PUBLIC_API_URL || '').trim().replace(/\/+$/,'');
 return base+'/api'+path;
}
export async function api<T>(path:string,body?:unknown,method?:string):Promise<T> {
 const controller=new AbortController();const timer=setTimeout(()=>controller.abort(),10000);
 try {
  const response=await fetch(apiUrl(path),{method:method || (body===undefined?'GET':'POST'),headers:body===undefined?{}:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body),signal:controller.signal});
  const expired=response.status===404&&path!=='/profile/extract'&&(/^\/plan\//.test(path)||/^\/profile\//.test(path)||path==='/assistant');
  const raw=await response.text();let data:unknown;
  try { data=JSON.parse(raw); } catch {
   if(!response.ok) throw new ApiError('We can’t reach your plan right now. Check that the backend is running, then try again.',response.status,'http',expired);
   throw new ApiError('We couldn’t read the server’s reply. Please try again.',response.status,'malformed');
  }
  if(!response.ok) {
   const message=typeof data==='object' && data!==null && 'message' in data && typeof data.message==='string' ? data.message : 'Something went wrong. Please try again.';
   throw new ApiError(response.status>=500?'We can’t reach your plan right now. Please try again.':message,response.status,'http',expired);
  }
  if(typeof data!=='object' || data===null || Array.isArray(data) || !validReply(path,data as Record<string,unknown>)) throw new ApiError('We couldn’t read the server’s reply. Please try again.',response.status,'malformed');
  return data as T;
 } catch(error) {
  if(error instanceof ApiError) throw error;
  if(controller.signal.aborted) throw new ApiError('The server took too long to reply. Please try again.',null,'timeout');
  throw new ApiError('We can’t reach your plan right now. Check that the backend is running, then try again.',null,'network');
 } finally { clearTimeout(timer); }
}
export const initialProfile:Profile={nationality:null,movingFrom:null,alreadyInUae:false,movingWithFamily:false,movingWithChildren:false,residenceStatus:'NOT_STARTED',hasEmiratesId:false,hasHousing:false,wantsToDrive:false,licenceCountry:null,bringingPet:false};
export function remember(s:Snapshot) { localStorage.setItem('life-setup-profile',s.profileId); }
