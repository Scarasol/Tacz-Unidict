#version 150

in vec2 vUv;

uniform vec2  u_Size;          // (2R, 2R)
uniform float u_RadiusOuter;   // 外半径 R
uniform float u_RadiusInner;   // 内半径 r（0=实心圆盘，>0=环形轮盘）
uniform float u_AA;            // 抗锯齿像素宽度（1.0~1.5）

uniform vec4  u_BaseColor;     // 圆盘颜色（深灰半透明）
uniform vec4  u_HighlightColor;// 高亮扇区颜色
uniform vec4  u_LineColor;     // 分割线颜色
uniform float u_LineWidthPx;   // 分割线宽度（像素）

uniform float u_Segments;      // 扇区数量 N
uniform float u_Selected;      // 当前高亮 index（-1 表示无）

out vec4 fragColor;

const float PI = 3.14159265358979323846;

void main() {
    // uv(0..1) -> 像素空间，以中心为原点
    vec2 p = (vUv - vec2(0.5)) * u_Size;
    float dist = length(p);

    // 圆环 mask（包含抗锯齿）
    float outer = 1.0 - smoothstep(u_RadiusOuter - u_AA, u_RadiusOuter + u_AA, dist);
    float inner = smoothstep(u_RadiusInner - u_AA, u_RadiusInner + u_AA, dist);
    float ringMask = outer * inner;

    if (ringMask <= 0.001) discard;

    // 角度：让 0 从“上方”开始，并顺时针增加
    float ang = atan(p.y, p.x);         // [-PI, PI]
    ang = ang + PI * 0.5;               // 0 对齐到“上”
    if (ang < 0.0) ang += 2.0 * PI;      // [0, 2PI)

    float n = max(u_Segments, 1.0);
    float sector = (2.0 * PI) / n;

    float idx = floor(ang / sector);

    // 高亮扇区
    float highlight = 0.0;
    if (u_Selected >= 0.0) {
        highlight = 1.0 - step(0.5, abs(idx - u_Selected)); // idx==selected => 1
    }

    // 分割线（把角度差换算到像素宽度）
    float m = mod(ang, sector);
    float d = min(m, sector - m); // 到最近边界的角度差
    float safeDist = max(dist, 1.0);
    float angWidth = u_LineWidthPx / safeDist; // 像素宽度转角度宽度
    float line = 1.0 - smoothstep(angWidth, angWidth * 1.8, d);

    // 组合颜色
    vec4 col = u_BaseColor;
    col = mix(col, u_HighlightColor, highlight);
    col = mix(col, u_LineColor, clamp(line, 0.0, 1.0));

    // 最终透明度乘 ringMask
    col.a *= ringMask;

    fragColor = col;
}
