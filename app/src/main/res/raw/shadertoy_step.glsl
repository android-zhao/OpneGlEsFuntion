#version 300 es
precision mediump float;

layout(location = 0) out vec4 outColor;

uniform vec2 iResolution;
in vec2 v_texCoord;


vec4 mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    fragColor = vec4(1.0,1.0,1.0,1.0);
    //创建uv坐标和将uv坐标转移到屏幕中间，通过将原来纹理坐标的中心在左下角的（0,0） ---数学变化----变化到屏幕中心，和顶点坐标的中心进行了重合
    vec2 uv = fragCoord;
    uv = (uv - 0.5 )*2.0;


    //todo  不是很理解此种行为为何能实现不把图像拉伸
    uv.x *= iResolution.x/iResolution.y;//为了防止屏幕的宽高比对绘制的有影响，对uv坐标进行变化，


    //计算每一个像素点到（0，0）的长度，并将这个值赋给 fragColor --->得到了一个 从屏幕中心到周围的灰度渐变图
    float d = length(uv);
    //对d进行自减操作，负数也会变成黑色，因此经过次步操作得到的黑色圆圈变大
    d -= 0.15;
    //取完绝对值之后，黑色中心最小的负数也会变成正数，但是非常接近d-0.15= 0的周边越黑 稍微远离就会逐渐变白
    d = abs(d);

    //staep函数参数2个，前一个参数是阈值，后一个参数是变量，当后面这个值大于前面的阈值时就取1 否则取0 ，因此就成了
    //当距离屏幕中心点的距离为0.15的一圈圆上在画一圈宽度为 0.01的圆圈,使用step会很尖锐，可替换成smoothstep
    //    d = step(0.01,d);

    //将小于0.0的 全部赋值为0，大于0.1的全部赋值为1，,0 -0.1之间的平滑的赋值
    d  = smoothstep(0.0,0.1,d);

    fragColor = vec4(d,d,d,1.0);

    return fragColor;
}


void main() {
    vec4 fragColor =  vec4(1.0,0.0,0.0,0.0);
    mainImage(fragColor,v_texCoord);
    outColor = fragColor;
}
