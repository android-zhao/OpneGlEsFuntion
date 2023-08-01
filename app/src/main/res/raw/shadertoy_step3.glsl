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
    //解答：---> 因为将纹理坐标全映射成屏幕中心为坐标原点的话，即相当于把纹理贴到一个正方形上面，实际上屏幕的长宽比 是不一样的即
    //有可能是 u > v 有可能是  此时将原来的u 坐标范围从 （-1，1 ）乘以 长宽比 ，即将坐标转换到了 （- iResolution.x/iResolution.y ~ iResolution.x/iResolution.y） 之间

    uv.x *= iResolution.x/iResolution.y;//为了防止屏幕的宽高比对绘制的有影响，对uv坐标进行变化，


    vec2 uv0 = fragCoord ;
    vec3 finnalCol = vec3(0.0);



    for(float i = 0.0;i<4.0;i++){
        //下面3步主要作用是将 fract分形之后的，将每一个分型小区域内部的原点调整成它自身内部的中间值
        // 把uv坐标调整到(0 ~4)之间,那么小数部分就会出现 4次 因此屏幕上会出现 4个 图像
        uv *= 1.5;
        //新的内置函数，作用是将输入的值输出位小数部分，去掉整数部分
        //即 输入为  0 -1 之间的小数  输出位0 -1之间的小数，输入为 0-2之间的小数 输出位 0- 2之间的小数
        uv = fract(uv);
        //对目前的uv 坐标 做 -0.5 的动作，将每一个小数部分的中心转移到当前区域的中心
        uv = uv - 0.5;

        //计算每一个像素点到（0，0）的长度，并将这个值赋给 fragColor --->得到了一个 从屏幕中心到周围的灰度渐变图
        float d = length(uv) * exp( -length(uv0) );
        //先对最原始的d的值 进行一个生成 颜色的操作
        //    vec3 col = palette(d);

        // 对输入的d 增加一个随着时间递增 的一个变化，会让col 产生一个周期性变化
        vec3 col = palette(length(uv0) + i *.4  + iTime *.4);

        //对d进行自减操作，  负数也会变成黑色，因此经过次步操作得到的黑色圆圈变大
        d = sin(d *8. +iTime )/8.;
        //取完绝对值之后，黑色中心最小的负数也会变成正数，但是非常接近d-0.15= 0的周边越黑 稍微远离就会逐渐变白
        d = abs(d);

        //staep函数参数2个，前一个参数是阈值，后一个参数是变量，当后面这个值大于前面的阈值时就取1 否则取0 ，因此就成了
        //当距离屏幕中心点的距离为0.15的一圈圆上在画一圈宽度为 0.01的圆圈,使用step会很尖锐，可替换成smoothstep
        //    d = step(0.01,d);

        //将小于0.0的 全部赋值为0，大于0.1的全部赋值为1，,0 -0.1之间的平滑的赋值
        //    d  = smoothstep(0.0,0.1,d);

//        d  = 0.02/d ;

        d = pow(0.01/d,1.2);

        //  对上面生成的颜色值 col 进行随着时间变化 做乘法运算 d做完倒数运算 之后的取值范围在（0 -1），对生成的 col 在做一个乘法运算
        //    col *= d;
        finnalCol += col *d;
    }
    fragColor = vec4(finnalCol,1.0);
    return fragColor;
}


void main() {
    vec4 fragColor =  vec4(1.0,0.0,0.0,0.0);
    mainImage(fragColor,v_texCoord);
    outColor = fragColor;
}
