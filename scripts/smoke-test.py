#!/usr/bin/env python3
import argparse,json,urllib.request,urllib.error
parser=argparse.ArgumentParser(description='Check the Life Setup demo through the backend or Astro proxy.')
parser.add_argument('--base-url',default='http://127.0.0.1:9000',help='Origin, for example http://127.0.0.1:4321')
BASE=parser.parse_args().base_url.rstrip('/')+'/api'
def call(path,body=None):
 req=urllib.request.Request(BASE+path,data=None if body is None else json.dumps(body).encode(),headers={'Content-Type':'application/json'})
 with urllib.request.urlopen(req,timeout=20) as response:return json.load(response)
assert call('/health')=={'status':'ok','mode':'prototype'}
s=call('/demo',{});id=s['profileId']
status=lambda plan,task:next(t['status'] for t in plan['plan']['tasks'] if t['definition']['id']==task)
assert status(s,'driving')=='BLOCKED'
assert s['plan']['nextBestTaskId']=='residence'
before=call('/assistant',{'profileId':id,'question':'Can I exchange my driving licence now?'})
assert before['answer'].startswith('Not yet.')
try:call('/plan/'+id+'/tasks/driving/complete',{});raise AssertionError('Blocked completion accepted')
except urllib.error.HTTPError as e:assert e.code==400
s=call('/plan/'+id+'/tasks/emirates-id/complete',{})
assert set(s['newlyReadyTaskIds'])=={'bank','driving'}
assert status(s,'driving')=='READY'
assert status(s,'bank')=='READY'
assert call('/plan/'+id+'/tasks/emirates-id/complete',{})['newlyReadyTaskIds']==[]
after=call('/assistant',{'profileId':id,'question':'Can I exchange my driving licence now?'})
assert after['answer'].startswith('Yes.')
assert 'confirm whether you qualify' in after['answer']
s=call('/plan/'+id+'/tasks/bank/complete',{})
s=call('/plan/'+id+'/tasks/emirates-id',{'action':'undo'})
assert status(s,'bank')=='BLOCKED'
assert status(s,'driving')=='BLOCKED'
assert call('/assistant',{'profileId':id,'question':'Can I exchange my driving licence now?'})['answer'].startswith('Not yet.')
print('HTTP demo smoke test passed against '+BASE)
