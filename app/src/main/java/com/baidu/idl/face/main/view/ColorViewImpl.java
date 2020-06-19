package com.baidu.idl.face.main.view;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * GL图形绘制渲染器
 */
public class ColorViewImpl {

    private int program1;
    private int textureI;
    private int tIindex;
    private float[] vertices;
    private int positionHandle1 = -1;
    private int coordHandle1 = -1;
    private int yhandle = -1;
    private int ytid = -1;
    private ByteBuffer verticebuffer;
    private ByteBuffer coordbuffer;
    private int videowidth = -1;
    private int videoheight = -1;
    private boolean isProgBuilt = false;

    public ColorViewImpl() {
        setup();
    }

    private void setup() {
        vertices = squareVertices;
        textureI = GLES20.GL_TEXTURE0;
        tIindex = 0;
        createBuffers(vertices);
    }

    private void createBuffers(float[] vert) {
        verticebuffer = ByteBuffer.allocateDirect(vert.length * 4);
        verticebuffer.order(ByteOrder.nativeOrder());
        verticebuffer.asFloatBuffer().put(vert);
        verticebuffer.position(0);
        if (coordbuffer == null) {
            coordbuffer = ByteBuffer.allocateDirect(coordVertices.length * 4);
            coordbuffer.order(ByteOrder.nativeOrder());
            coordbuffer.asFloatBuffer().put(coordVertices);
            coordbuffer.position(0);
        }
    }

    public void drawSelf() {
        // 将program加入OpenGL ES环境中
        GLES20.glUseProgram(program1);
        //      ShaderUtil.checkGlError("glUseProgram");
        GLES20.glVertexAttribPointer(positionHandle1, 2, GLES20.GL_FLOAT, false, 8, verticebuffer);
        //      ShaderUtil.checkGlError("glVertexAttribPointer mPositionHandle");
        GLES20.glEnableVertexAttribArray(positionHandle1);
        GLES20.glVertexAttribPointer(coordHandle1, 2, GLES20.GL_FLOAT, false, 8, coordbuffer);
        //      ShaderUtil.checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(coordHandle1);
        // bind textures
        GLES20.glActiveTexture(textureI);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ytid);
        GLES20.glUniform1i(yhandle, tIindex);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisableVertexAttribArray(positionHandle1);
        GLES20.glDisableVertexAttribArray(coordHandle1);
        GLES20.glFinish();
    }

    private static float[] squareVertices = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private static float[] coordVertices = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    /**
     * 用于渲染形状的顶点的OpenGLES图形代码
     */
    private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" + "attribute vec2 a_texCoord;\n" + "varying vec2 tc;\n" + "void main() {\n" + "gl_Position = vPosition;\n" + "tc = a_texCoord;\n" + "}\n";

    /**
     * 用于渲染形状的外观（颜色或纹理）的OpenGLES代码
     */
    private static final String FRAGMENT_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n" + "varying vec2 tc;\n" + "void main() {\n" + "gl_FragColor = texture2D(tex_y,tc);\n" + "}\n";

    public boolean isProgramBuilt() {
        return isProgBuilt;
    }

    public void buildProgram() {
        if (program1 <= 0) {
            program1 = ImiShaderUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        }
        // 获取指向vertex shader的成员vPosition的 handle
        positionHandle1 = GLES20.glGetAttribLocation(program1, "vPosition");
        ImiShaderUtil.checkGlError("glGetAttribLocation vPosition");
        if (positionHandle1 == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        coordHandle1 = GLES20.glGetAttribLocation(program1, "a_texCoord");
        ImiShaderUtil.checkGlError("glGetAttribLocation a_texCoord");
        if (coordHandle1 == -1) {
            throw new RuntimeException("Could not get attribute location for a_texCoord");
        }
        yhandle = GLES20.glGetUniformLocation(program1, "tex_y");
        ImiShaderUtil.checkGlError("glGetUniformLocation tex_y");
        if (yhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_y");
        }
        isProgBuilt = true;
    }

    public void buildTextures(Buffer rgbBuffer, int width, int height) {
        boolean videoSizeChanged = (width != videowidth || height != videoheight);
        if (videoSizeChanged) {
            videowidth = width;
            videoheight = height;
        }
        if (ytid < 0 || videoSizeChanged) {
            if (ytid >= 0) {
                GLES20.glDeleteTextures(1, new int[]{ytid}, 0);
                ImiShaderUtil.checkGlError("glDeleteTextures");
            }
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            ImiShaderUtil.checkGlError("glGenTextures");
            ytid = textures[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ytid);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ytid);
        ImiShaderUtil.checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, videowidth, videoheight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, rgbBuffer);
        ImiShaderUtil.checkGlError("glTexImage2D");

    }
}
