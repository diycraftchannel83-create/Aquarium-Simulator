#!/usr/bin/env python3
"""Generate transparent, looping fish GIFs that visually correspond to models/fish/*.glb.
Version: 1.0.1.
The GLBs are static meshes, so this produces lightweight 2D swim cycles for the current
Canvas/Coil renderer. Run with Pillow installed.
"""
from PIL import Image, ImageDraw
from pathlib import Path
import math

ROOT = Path(__file__).resolve().parents[1] / "app/src/main/assets/aquarium/fish"
ROOT.mkdir(parents=True, exist_ok=True)
W, H, SCALE, FRAMES = 320, 200, 3, 12

SPECIES = {
    "clownfish": dict(body=(224,91,39), belly=(255,176,91), fin=(244,126,54), ink=(42,31,27), stripe=(255,249,229), rx=65, ry=39),
    "blue_tang": dict(body=(20,105,207), belly=(54,164,232), fin=(16,67,139), ink=(10,39,91), stripe=(255,218,55), rx=70, ry=35),
    "angelfish": dict(body=(230,72,34), belly=(255,142,53), fin=(36,61,119), ink=(45,31,30), stripe=(255,224,175), rx=48, ry=45),
    "neon_tetra": dict(body=(40,163,181), belly=(93,218,209), fin=(222,70,89), ink=(25,65,101), stripe=(91,239,255), rx=61, ry=27),
}

def pts(values):
    return [(round(x*SCALE), round(y*SCALE)) for x,y in values]

def render(name, palette, frame):
    sw, sh = W*SCALE, H*SCALE
    im = Image.new("RGBA", (sw, sh), (0,0,0,0))
    d = ImageDraw.Draw(im)
    phase = 2*math.pi*frame/FRAMES
    tail = math.sin(phase)*9
    fin_wave = math.sin(phase+1.1)*4
    cx, cy = 158, 100 + math.sin(phase)*1.3
    rx, ry = palette["rx"], palette["ry"]
    body, belly, fin, ink, stripe = (palette[k] for k in ("body","belly","fin","ink","stripe"))

    # Flowing dorsal/anal fins and a forked tail silhouette.
    if name == "angelfish":
        dorsal=[(118,cy-22),(134,cy-68-fin_wave),(196,cy-62-fin_wave),(211,cy-26)]
        anal=[(122,cy+24),(145,cy+66+fin_wave),(198,cy+59+fin_wave),(211,cy+25)]
    else:
        dorsal=[(115,cy-21),(143,cy-48-fin_wave),(205,cy-42-fin_wave),(215,cy-18)]
        anal=[(118,cy+21),(146,cy+43+fin_wave),(198,cy+39+fin_wave),(214,cy+18)]
    if name == "angelfish":
        tailpts=[(112,cy),(78,cy-25+tail),(59,cy-39+tail),(66,cy),(59,cy+39+tail),(78,cy+25+tail)]
    else:
        tailpts=[(98,cy),(65,cy-24+tail),(45,cy-35+tail),(53,cy),(45,cy+35+tail),(65,cy+24+tail)]
    d.polygon(pts(dorsal), fill=fin+(255,), outline=ink+(255,), width=2*SCALE)
    d.polygon(pts(anal), fill=fin+(255,), outline=ink+(255,), width=2*SCALE)
    d.polygon(pts(tailpts), fill=fin+(255,), outline=ink+(255,), width=2*SCALE)

    # Elliptical body with vertical color shading clipped to the actual mesh silhouette.
    mask=Image.new("L",(sw,sh),0)
    ImageDraw.Draw(mask).ellipse((int((cx-rx)*SCALE),int((cy-ry)*SCALE),int((cx+rx)*SCALE),int((cy+ry)*SCALE)),fill=255)
    grad=Image.new("RGBA",(sw,sh),(0,0,0,0))
    gp=grad.load()
    for y in range(max(0,int((cy-ry)*SCALE)),min(sh,int((cy+ry+1)*SCALE))):
        t=(y/SCALE-cy+ry)/(2*ry)
        color=tuple(round(body[i]*(1-t)+belly[i]*t) for i in range(3))+(255,)
        for x in range(max(0,int((cx-rx)*SCALE)),min(sw,int((cx+rx+1)*SCALE))):
            gp[x,y]=color
    grad.putalpha(mask)
    im.alpha_composite(grad)
    d=ImageDraw.Draw(im)
    d.ellipse((int((cx-rx)*SCALE),int((cy-ry)*SCALE),int((cx+rx)*SCALE),int((cy+ry)*SCALE)),outline=ink+(255,),width=2*SCALE)

    # Distinctive species markings from the GLB catalog identity.
    if name == "clownfish":
        for bx in (cx-25,cx+13):
            d.rounded_rectangle((int((bx-8)*SCALE),int((cy-ry-2)*SCALE),int((bx+7)*SCALE),int((cy+ry+2)*SCALE)),radius=6*SCALE,fill=stripe+(255,),outline=ink+(255,),width=3*SCALE)
    elif name == "blue_tang":
        d.arc((int((cx-50)*SCALE),int((cy-43)*SCALE),int((cx+48)*SCALE),int((cy+43)*SCALE)),205,340,fill=ink+(255,),width=7*SCALE)
        d.arc((int((cx-42)*SCALE),int((cy-36)*SCALE),int((cx+38)*SCALE),int((cy+36)*SCALE)),205,340,fill=(12,37,91,255),width=3*SCALE)
        d.polygon(pts([(cx+rx-6,cy-16),(cx+rx+15,cy-9),(cx+rx+15,cy+9),(cx+rx-6,cy+16)]),fill=stripe+(255,))
    elif name == "angelfish":
        for bx in (cx-23,cx+1,cx+24):
            d.polygon(pts([(bx-5,cy-ry+5),(bx+2,cy-ry+4),(bx+6,cy+ry-5),(bx-2,cy+ry-5)]),fill=ink+(255,))
    else:
        d.line(pts([(cx-38,cy-3),(cx+39,cy-3)]),fill=stripe+(255,),width=5*SCALE)
        d.line(pts([(cx-30,cy+5),(cx+38,cy+7)]),fill=(235,65,90,255),width=5*SCALE)

    # Gill, translucent pectoral fin, eye, mouth, and small scale highlights.
    d.arc((int((cx-2)*SCALE),int((cy-25)*SCALE),int((cx+31)*SCALE),int((cy+27)*SCALE)),65,295,fill=ink+(220,),width=2*SCALE)
    d.ellipse((int((cx-2)*SCALE),int((cy-5+fin_wave)*SCALE),int((cx+19)*SCALE),int((cy+16+fin_wave)*SCALE)),fill=fin+(170,),outline=ink+(220,),width=SCALE)
    ex,ey=cx+44,cy-9
    d.ellipse((int((ex-7)*SCALE),int((ey-7)*SCALE),int((ex+7)*SCALE),int((ey+7)*SCALE)),fill=(255,255,245,255),outline=ink+(255,),width=2*SCALE)
    d.ellipse((int((ex-3)*SCALE),int((ey-4)*SCALE),int((ex+4)*SCALE),int((ey+4)*SCALE)),fill=(12,23,35,255))
    d.ellipse((int((ex-2)*SCALE),int((ey-4)*SCALE),int(ex*SCALE),int((ey-2)*SCALE)),fill=(255,255,255,255))
    d.arc((int((cx+56)*SCALE),int((cy+3)*SCALE),int((cx+69)*SCALE),int((cy+12)*SCALE)),190,330,fill=ink+(255,),width=2*SCALE)
    for ox,oy in [(-25,-13),(-10,-20),(8,-17),(25,-13),(-20,9),(1,12),(19,8)]:
        d.arc((int((cx+ox-4)*SCALE),int((cy+oy-3)*SCALE),int((cx+ox+4)*SCALE),int((cy+oy+4)*SCALE)),200,330,fill=(255,255,255,90),width=SCALE)
    d.arc((int((cx-rx+9)*SCALE),int((cy-ry+4)*SCALE),int((cx+rx-8)*SCALE),int((cy+ry-5)*SCALE)),195,280,fill=(255,255,255,110),width=2*SCALE)
    return im.resize((W,H),Image.Resampling.LANCZOS)

for species, palette in SPECIES.items():
    frames=[render(species,palette,i) for i in range(FRAMES)]
    frames[0].save(ROOT/f"{species}.gif",save_all=True,append_images=frames[1:],duration=85,loop=0,disposal=2,optimize=True)
    print(f"Generated {species}.gif")
