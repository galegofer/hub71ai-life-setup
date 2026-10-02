import {useEffect,useId,useRef,useState} from 'react';
import {countryName,searchKey,useCountries} from '../lib/countries';
export default function CountryCombobox({label,value,onChange,licence=false}:{label:string;value:string|null;onChange:(value:string|null)=>void;licence?:boolean}){
 const {countries,error,loading,retry}=useCountries();const id=useId();const input=useRef<HTMLInputElement>(null);const box=useRef<HTMLDivElement>(null);
 const [open,setOpen]=useState(false);const [query,setQuery]=useState('');const [active,setActive]=useState(0);
 const selected=countryName(value,countries);const needle=searchKey(query);
 const matches=countries.filter(c=>!needle||[c.name,...c.aliases].some(name=>searchKey(name).includes(needle)));
 const options=[...matches.map(c=>({value:c.code as string|null,label:c.name})),...(!needle||searchKey('Unknown / not sure').includes(needle)?[{value:null,label:'Unknown / not sure'}]:[]),...(licence&&(!needle||searchKey('I don’t have a foreign driving licence').includes(needle))?[{value:'NONE',label:'I don’t have a foreign driving licence'}]:[])];
 const index=Math.max(0,Math.min(active,Math.max(0,options.length-1)));
 useEffect(()=>{if(open)box.current?.querySelector<HTMLElement>('[data-active="true"]')?.scrollIntoView({block:'nearest'});},[index,open,query]);
 function choose(next:string|null){onChange(next);setOpen(false);setQuery('');input.current?.focus();}
 return <div className="country-control" ref={box}><label htmlFor={id}>{label}</label><input ref={input} id={id} role="combobox" aria-autocomplete="list" aria-expanded={open&&!loading&&!error} aria-controls={id+'-list'} aria-activedescendant={open&&options.length&&!loading&&!error?id+'-option-'+index:undefined} value={open?query:selected} placeholder="Search countries, or choose Unknown" onFocus={()=>{setOpen(true);setQuery('');setActive(0);}} onChange={e=>{setQuery(e.target.value);setOpen(true);setActive(0);}} onBlur={()=>{setOpen(false);setQuery('');}} onKeyDown={e=>{
  if(e.key==='Escape'){e.preventDefault();setOpen(false);setQuery('');}
  else if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();setOpen(true);setActive(n=>e.key==='ArrowDown'?Math.min(options.length-1,n+1):Math.max(0,n-1));}
  else if(e.key==='Enter'&&open){e.preventDefault();if(options[index])choose(options[index].value);}
 }}/><div className="country-actions"><button type="button" onClick={()=>choose(null)}>Unknown / not sure</button>{value!==null&&<button type="button" onClick={()=>choose(null)}>Clear</button>}{licence&&<button type="button" onClick={()=>choose('NONE')}>I don’t have a foreign driving licence</button>}</div>
 {loading&&<p className="quiet" role="status">Loading countries…</p>}{error&&<div className="error" role="alert">{error}<button type="button" className="text-link" onClick={retry}>Retry country list</button></div>}
 {open&&!loading&&!error&&<div id={id+'-list'} role="listbox" aria-label={label+' options'} className="country-list">{options.length?options.map((option,i)=><div key={option.value??'unknown'} id={id+'-option-'+i} role="option" aria-selected={value===option.value} data-active={i===index} onPointerDown={e=>{e.preventDefault();choose(option.value);}}>{option.label}</div>):<p className="quiet" role="status">No countries match. Try another name or choose Unknown.</p>}</div>}
 </div>;
}
