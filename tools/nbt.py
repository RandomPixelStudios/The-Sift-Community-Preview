import gzip, struct, io
# Minimal NBT reader/writer. Values are (tag_id, value) tuples to preserve types.
def _r(f, n): return f.read(n)
def read_payload(f, t):
    if t == 1: return struct.unpack('>b', _r(f,1))[0]
    if t == 2: return struct.unpack('>h', _r(f,2))[0]
    if t == 3: return struct.unpack('>i', _r(f,4))[0]
    if t == 4: return struct.unpack('>q', _r(f,8))[0]
    if t == 5: return struct.unpack('>f', _r(f,4))[0]
    if t == 6: return struct.unpack('>d', _r(f,8))[0]
    if t == 7: n=struct.unpack('>i',_r(f,4))[0]; return bytearray(_r(f,n))
    if t == 8: n=struct.unpack('>H',_r(f,2))[0]; return _r(f,n).decode('utf-8')
    if t == 9:
        et=_r(f,1)[0]; n=struct.unpack('>i',_r(f,4))[0]
        return (et,[read_payload(f,et) for _ in range(n)])
    if t == 10:
        d={}
        while True:
            tt=_r(f,1)[0]
            if tt==0: return d
            n=struct.unpack('>H',_r(f,2))[0]; name=_r(f,n).decode('utf-8')
            d[name]=(tt,read_payload(f,tt))
    if t == 11: n=struct.unpack('>i',_r(f,4))[0]; return list(struct.unpack('>%di'%n,_r(f,4*n)))
    if t == 12: n=struct.unpack('>i',_r(f,4))[0]; return list(struct.unpack('>%dq'%n,_r(f,8*n)))
    raise ValueError(t)
def write_payload(f,t,v):
    if t==1: f.write(struct.pack('>b',v))
    elif t==2: f.write(struct.pack('>h',v))
    elif t==3: f.write(struct.pack('>i',v))
    elif t==4: f.write(struct.pack('>q',v))
    elif t==5: f.write(struct.pack('>f',v))
    elif t==6: f.write(struct.pack('>d',v))
    elif t==7: f.write(struct.pack('>i',len(v))); f.write(bytes(v))
    elif t==8: b=v.encode('utf-8'); f.write(struct.pack('>H',len(b))); f.write(b)
    elif t==9:
        et,items=v
        if not items and et==0: et=0
        f.write(bytes([et])); f.write(struct.pack('>i',len(items)))
        for i in items: write_payload(f,et,i)
    elif t==10:
        for k,(tt,vv) in v.items():
            f.write(bytes([tt])); b=k.encode('utf-8'); f.write(struct.pack('>H',len(b))); f.write(b); write_payload(f,tt,vv)
        f.write(b'\x00')
    elif t==11: f.write(struct.pack('>i',len(v))); f.write(struct.pack('>%di'%len(v),*v))
    elif t==12: f.write(struct.pack('>i',len(v))); f.write(struct.pack('>%dq'%len(v),*v))
def load(path):
    f=io.BytesIO(gzip.open(path).read()); t=f.read(1)[0]; n=struct.unpack('>H',f.read(2))[0]; name=f.read(n).decode()
    return name, read_payload(f,t)
def save(path,name,root):
    f=io.BytesIO(); f.write(b'\x0a'); b=name.encode(); f.write(struct.pack('>H',len(b))); f.write(b); write_payload(f,10,root)
    with gzip.open(path,'wb') as g: g.write(f.getvalue())
