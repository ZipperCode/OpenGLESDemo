attribute vec4 vPosition;
attribute vec2 vCoordinate;
varying vec2 aCoordinate;
uniform mat4 vMatrix;
varying vec2 aFBOTextureCoord;

void main() {
    gl_Position = vMatrix * vPosition;
    aCoordinate = vCoordinate;
    aFBOTextureCoord = vec2(vCoordinate.x, 1.0 - vCoordinate.y);
}
根据上下文理解混淆的变量并修改，同时添加注释，注释需要包含重命名前的变量名称，类名可以不用改