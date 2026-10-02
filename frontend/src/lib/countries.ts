import {useEffect,useState} from 'react';
import {api,ApiError} from './api';
export interface Country {code:string;name:string;aliases:string[]}
let cached:Country[]|null=null;
let pending:Promise<Country[]>|null=null;
export function getCountries():Promise<Country[]> {
 if(cached)return Promise.resolve(cached);
 if(!pending)pending=api<{countries:Country[]}>('/countries').then(data=>{
  if(!Array.isArray(data.countries)||!data.countries.length||!data.countries.every(c=>/^[A-Z]{2}$/.test(c.code)&&typeof c.name==='string'&&Array.isArray(c.aliases)))throw new ApiError('We couldn’t read the country list. Please try again.',200,'malformed');
  cached=[...data.countries].sort((a,b)=>a.name.localeCompare(b.name,'en'));return cached;
 }).finally(()=>{pending=null;});
 return pending;
}
export function useCountries(){
 const [countries,setCountries]=useState<Country[]>(cached||[]);const [error,setError]=useState('');const [loading,setLoading]=useState(!cached);const [attempt,setAttempt]=useState(0);
 useEffect(()=>{let active=true;setLoading(true);setError('');getCountries().then(data=>{if(active)setCountries(data);}).catch(e=>{if(active)setError((e as Error).message);}).finally(()=>{if(active)setLoading(false);});return()=>{active=false;};},[attempt]);
 return {countries,error,loading,retry:()=>setAttempt(n=>n+1)};
}
export const searchKey=(text:string)=>text.normalize('NFD').replace(/\p{M}/gu,'').toLocaleLowerCase('en').trim();
export function countryName(value:string|null,countries:Country[]):string {return value==='NONE'?'No foreign driving licence':value===null?'Unknown / not sure':countries.find(c=>c.code===value)?.name||'Saved country — awaiting lookup';}
