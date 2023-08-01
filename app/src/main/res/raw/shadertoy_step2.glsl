#version 300 es
precision mediump float;

layout(location = 0) out vec4 outColor;

uniform vec2 iResolution;
uniform float iTime;
in vec2 v_texCoord;

//得出一个颜色 通过 函数 a + b*cos( 6.28318*(c*t+d) ) 输出一个颜色值;
vec3 palette( float t ) {
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263,0.416,0.557);

    return a + b*cos( 6.28318*(c*t+d) );
}

vec4 mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    fragColor = vec4(1.0,1.0,1.0,1.0);
    //创建uv坐标和将uv坐标转移到屏幕中间，通过将原来纹理坐标的中心在左下角的（0,0） ---数学变化----变化到屏幕中心，和顶点坐标的中心进行了重合
    vec2 uv = fragCoord;
    uv = (uv - 0.5 )*2.0;


    //todo  不是很理解此种行为为何能实现不把图像拉伸
    uv.x *= iResolution.x/iResolution.y;//为了防止屏幕的宽高比对绘制的有影响，对uv坐标进行变化，
    //计算每一个像素点到（0，0）的长度，并将这个值赋给 fragColor --->得到了一个 从屏幕中心到周围的灰度渐变图
    float d = length(uv);
    //先对最原始的d的值 进行一个生成 颜色的操作
//    vec3 col = palette(d);

    // 对输入的d 增加一个随着时间递增 的一个变化，会让col 产生一个周期性变化
    vec3 col = palette(d + iTime);

    //对d进行自减操作，  负数也会变成黑色，因此经过次步操作得到的黑色圆圈变大
    d = sin(d *8. +iTime )/8.;
    //取完绝对值之后，黑色中心最小的负数也会变成正数，但是非常接近d-0.15= 0的周边越黑 稍微远离就会逐渐变白
    d = abs(d);

    //staep函数参数2个，前一个参数是阈值，后一个参数是变量，当后面这个值大于前面的阈值时就取1 否则取0 ，因此就成了
    //当距离屏幕中心点的距离为0.15的一圈圆上在画一圈宽度为 0.01的圆圈,使用step会很尖锐，可替换成smoothstep
    //    d = step(0.01,d);

    //将小于0.0的 全部赋值为0，大于0.1的全部赋值为1，,0 -0.1之间的平滑的赋值
//    d  = smoothstep(0.0,0.1,d);

    d  = 0.02/d ;

//  对上面生成的颜色值 col 进行随着时间变化 做乘法运算 d做完倒数运算 之后的取值范围在（0 -1），对生成的 col 在做一个乘法运算
    col *= d;
    fragColor = vec4(col,1.0);

    return fragColor;
}


void main() {
    vec4 fragColor =  vec4(1.0,0.0,0.0,0.0);
    mainImage(fragColor,v_texCoord);
    outColor = fragColor;
}
