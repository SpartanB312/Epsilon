#ifdef GL_ES
precision mediump float;
#endif

// glslsandbox uniforms
uniform float time;
uniform vec2 resolution;
uniform vec2 mouse;

// shadertoy emulation
#define iTime time
#define iResolution resolution

// --------[ Original ShaderToy begins here ]---------- //
#define AA (25./max(iResolution.x, iResolution.y))
#define HASHSCALE3 vec3(.1031, .1030, .0973)

float hash( float n ) { return fract(sin(n)*123.456789); }

float noise( in vec3 p )
{
    vec3 fl = floor( p );
    vec3 fr = fract( p );
    fr = fr * fr * ( 3.0 - 2.0 * fr );

    float n = fl.x + fl.y * 157.0 + 113.0 * fl.z;
    return mix( mix( mix( hash( n +   0.0), hash( n +   1.0 ), fr.x ),
                     mix( hash( n + 157.0), hash( n + 158.0 ), fr.x ), fr.y ),
                mix( mix( hash( n + 113.0), hash( n + 114.0 ), fr.x ),
                     mix( hash( n + 270.0), hash( n + 271.0 ), fr.x ), fr.y ), fr.z );
}

float fbm( in vec2 p, float t )
{
    float f;
    f  = 0.5000 * noise( vec3( p, t ) ); p *= 2.1;
    f += 0.2500 * noise( vec3( p, t ) ); p *= 2.2;
    f += 0.1250 * noise( vec3( p, t ) ); p *= 2.3;
    f += 0.0625 * noise( vec3( p, t ) );
    return f;
}

float fbm(float x){
    float y = 0.;
    // Properties
    const int octaves = 1;
    float lacunarity = 2.0;
    float gain = 0.5;
    //
    // Initial values
    float amplitude = 0.5;
    float frequency = 1.;
    //
    // Loop of octaves
    for (int i = 0; i < octaves; i++) {
        y += amplitude * noise(vec3(frequency * x));
        frequency *= lacunarity;
        amplitude *= gain;
    }

    return y;
}

vec2 rotate( in vec2 uv, float a)
{
    float c = cos( a );
    float s = sin( a );
    return vec2( c * uv.x - s * uv.y, s * uv.x + c * uv.y );
}

vec3 hash33(vec3 p3){
        p3 = fract(p3 * HASHSCALE3);
    p3 += dot(p3, p3.yxz+19.19);
    return fract((p3.xxy + p3.yxx)*p3.zyx);

}

vec2 voronoi(in vec3 x){
    vec3 p = floor( x );
    vec3 f = fract( x );

	float id;
    vec2 res = vec2( 100.0 );
    for( int k=-1; k<=1; k++ )
    	for( int j=-1; j<=1; j++ )
    		for( int i=-1; i<=1; i++ ){
                vec3 b = vec3( float(i), float(j), float(k) );
                vec3 r = vec3( b ) - f + hash33( p + b );
                float d = dot( r, r );

                if( d < res.x ){
                    id = dot(p + b, vec3(20.31, 517., 113.));
                    res = vec2(d, res.x);
                }else if(d < res.y){
                    res.y = d;
                }
    }
    return vec2(res.x, abs(id));
}

const float PI = acos(-1.);

const vec3 C_BG_1 = vec3(107., 89., 167.)/255.;
const vec3 C_BG_2 = vec3(244., 142., 112.)/255.;
const vec3 WHITE = vec3(1.);

const vec2 MAIN_CONTOUR_BOUND = vec2(2.);
float mainContour(vec2 uv){
    return mix(abs(uv.y) - MAIN_CONTOUR_BOUND.y,
               length(abs(uv) - vec2(MAIN_CONTOUR_BOUND.x, 0.)) - MAIN_CONTOUR_BOUND.y,
               step(MAIN_CONTOUR_BOUND.x, abs(uv.x)));
}

const vec3 C_MOON_1 = vec3(1.);
const vec3 C_MOON_2 = vec3(253., 226., 187.)/255.;
const float MOON_RAD = 1.9;
vec4 moon(vec2 uv, float phase){
    vec2 pos = uv - vec2(mix(-MAIN_CONTOUR_BOUND.x, MAIN_CONTOUR_BOUND.x, phase), 0.);
    float alpha = smoothstep(0., AA, length(pos) - MOON_RAD);
    vec3 clr = mix(C_MOON_1, C_MOON_2, phase);

    float ang = phase * PI * .5;
    pos *= mat2(cos(ang), -sin(ang), sin(ang), cos(ang));
    vec2 v = voronoi(vec3(pos, 1.));
    float size = hash33(vec3(v.y + .29)).x * .2;
    float craters = -.2 * smoothstep(size + AA * size, size, v.x);
    vec2 v2 = voronoi(vec3(pos + vec2(-.5 * size, 0.), 1.));
    size = hash33(vec3(v.y + .29)).x * .2;
    craters += .1 * smoothstep(size + AA, size, v2.x) * smoothstep(size + AA, size, v.x);
    craters *= 1. - phase;

    return vec4(clr + craters, alpha);
}

vec2 mpos1 = vec2(2.5, 0.75);
vec2 mpos2 = vec2(4., 2.5);
float moonsize = 1.;
float moon2(vec2 uv, float phase){
    vec2 pos = uv - mix(mpos1, mpos2, phase);
    pos = rotate(pos, -phase);
    float moon = clamp(smoothstep(0., AA, length(pos) - moonsize), 0., 1.);

    pos += vec2(.35, -.2);
    moon += clamp(smoothstep(AA, 0., length(pos) - moonsize), 0., 1.);

    return clamp(moon, 0., 1.);
}

vec2 spos1 = vec2(-2.5, 0.5);
vec2 spos2 = vec2(-.5, -3.5);
float sunsize = .1;
vec4 sun(vec2 uv, float phase){
    vec2 pos = uv - mix(spos1, spos2, 1. - phase);
    pos = rotate(pos, -phase);
    float sun = length(pos) - sunsize;

    float a = floor((sun)/.25);

    return vec4(C_MOON_2, clamp(1. - a * .25, 0., 1.));
}

#define HASHSCALE1 .1031
float hash11(float p)
{
    vec3 p3  = fract(vec3(p) * HASHSCALE1);
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 l11 = vec3(107., 57., 148.)/255.;
vec3 l12 = vec3(231., 107., 123.)/255.;
vec3 l21 = vec3(105., 49., 124.)/255.;
vec3 l22 = vec3(181., 74., 107.)/255.;
vec3 l31 = vec3(90., 41., 107.)/255.;
vec3 l32 = vec3(140., 41., 74.)/255.;
vec4 landscape(vec2 uv, float phase){
    vec3 res;
    float alpha;
    {
        float scale = 10.;
        float x = fract(uv.x * scale);
        float fx = floor(uv.x * scale);
        float cx = ceil(uv.x * scale);
        float f = mix(hash11(fx), hash11(cx), x) + sin(uv.x * (2. + step(0., uv.x) * .5) + 1.5) * .75 * abs(uv.x) - (2. + .1 * step(uv.x, 0.));
        alpha = smoothstep(f, f + AA * 2., uv.y);
        res = mix(l11, l12, phase);
    }

    float f2 = fbm(uv.x * .5 + 50.) * 2.5 + (uv.x) * .2 - 2.;
    float s2 = smoothstep(f2, f2 + AA, uv.y);
    res = mix(res, mix(l31, l32, phase), s2);
    alpha = min(alpha, s2);


    float f1 = fbm(uv.x * .5 - 10.) * 2.5 - (uv.x) * .25 - 2.;
    float s1 = smoothstep(f1, f1 + AA, uv.y);
    res = mix(mix(l21, l22, phase), res, s1);
    alpha = min(alpha, s1);

    return vec4(res, alpha);
}

float hash12(vec2 p)
{
        vec3 p3  = fract(vec3(p.xyx) * HASHSCALE1);
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

float fs(vec2 uv, float phase){
    uv.x += uv.y;
    uv.x += iTime * 20.;
    uv *= vec2(.15, 200.);
    float star = hash12(floor(uv));
    star = pow(1. - fract(uv.x), 4.) * step(.9995, star);
    return star * (1. - phase);
}

const int star_iterations = 10;
float doBackgroundStars(in vec3 dir){
    vec3 n  = abs( dir );
    vec2 uv = ( n.x > n.y && n.x > n.z ) ? dir.yz / dir.x:
              ( n.y > n.x && n.y > n.z ) ? dir.zx / dir.y:
                                           dir.xy / dir.z;

    float f = 0.0;

    for( int i = 0 ; i < star_iterations; ++i )
    {
        uv = rotate( 1.07 * uv + vec2( 0.7 ), 0.5 );

        float t = 10. * uv.x * uv.y + iTime;
        vec2 u = cos( 100. * uv ) * fbm( 10. * uv, 0.0 );
        f += smoothstep( 0.5, 0.55, u.x * u.y ) * ( 0.25 * sin( t ) + 0.75 );
    }
    return f;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ){
    vec2 uv = (2.*fragCoord-iResolution.xy)/iResolution.y * 3.;
    float phase = mouse.x;

    vec3 bg = mix(C_BG_1, C_BG_2, phase);
    vec3 foreground = mix(C_BG_1, C_BG_2, pow(phase, 3.));

    vec3 finalClr = bg;
    float mainCntr = mainContour(uv);
    finalClr *= .85 + .15 * smoothstep(0., .5, mainCntr);
    float mask = smoothstep(0., AA, mainCntr);
    finalClr = mix(foreground, finalClr, mask);
    finalClr += doBackgroundStars(vec3(uv * .2, 1.)) * (1. - mask) * (1. - phase);
    finalClr += fs(rotate(uv, -PI * .25), phase) * (1. - mask) * (1. - phase);
    finalClr = mix(finalClr, WHITE, (1. - moon2(uv, phase)) * (1. - mask));
    vec4 sun = sun(uv, phase);
    finalClr = mix(finalClr, sun.rgb, sun.a * (1. - mask));

    vec4 landscape = landscape(uv, phase);
    finalClr = mix(finalClr, landscape.rgb, (1. - landscape.a) * (1. - mask));

    vec4 moon = moon(uv, phase);
    finalClr = mix(moon.rgb, finalClr, moon.a);

    fragColor = vec4(finalClr, 1.);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}