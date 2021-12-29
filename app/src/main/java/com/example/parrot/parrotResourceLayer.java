package com.example.parrot;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class parrotResourceLayer {
    private int BYTESIZE = 4;
    private Context context;
    public FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    public static final int COORDS_PER_VERTEX = 3;
    // lists dedicated to pure coordinate/vertex data
    public ArrayList<Float> vertexlist = new ArrayList<Float>();
    public ArrayList<Float> uvlist = new ArrayList<Float>();
    public ArrayList<Float> normalslist = new ArrayList<Float>();
    // lists dedicated to indices
    public ArrayList<Integer> vertexindices = new ArrayList<Integer>();
    public ArrayList<Integer> uvindices = new ArrayList<Integer>();
    public ArrayList<Integer> normalsindices = new ArrayList<Integer>();
    public int vsize = vertexlist.size();

    // Use to access and set the view transformation
    public int vPMatrixHandle;
    public String vertexShaderCode = "";
    public String fragmentShaderCode = "";

    public int vertexCount = vsize / COORDS_PER_VERTEX;
    public int vertexStride = COORDS_PER_VERTEX * BYTESIZE; // 4 bytes per vertex
    public int mProgram = 0;

    //A context is required to do anything with the raw resource files. Make sure to provide
    //context from an activity upon instantiation.
    public parrotResourceLayer(Context maincontext) {
        context = maincontext;
        try {
            importShaderFromFile(R.raw.vshader);
            importShaderFromFile(R.raw.fshader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateVertexBuffer();
        updateShaderObjects();
    }

    //Imports mesh from a file from a provided file id, if bufferReset parameter
    //is true then the loaded vertexlists and buffers will be reset
    public void importMeshFromFile (int id, boolean bufferReset) throws IOException {
        InputStream is = context.getResources().openRawResource(id);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        ArrayList<Float> tvertexlist, tuvlist, tnormalslist;
        tvertexlist = tuvlist = tnormalslist = new ArrayList<Float>();

        if(bufferReset) {
            vertexlist.clear();
            uvlist.clear();
            normalslist.clear();
            vertexindices.clear();
            uvindices.clear();
            normalsindices.clear();
        }
        for (String line; (line = r.readLine()) != null; ) {
            if(line.length() <= 3) continue;
            else if(line.charAt(0) == 'v' && line.charAt(1) == ' ') {
                String numstr[] = line.split("v ");
                String[] coordvals = numstr[1].split(" ");
                for(String ch : coordvals) {
                    tvertexlist.add(Float.parseFloat(ch));
                }
            }
            else if(line.charAt(0) == 'v' && line.charAt(1) == 'n') {
                String numstr[] = line.split("vn ");
                String[] coordvals = numstr[1].split(" ");
                for(String ch : coordvals) {
                    tnormalslist.add(Float.parseFloat(ch));
                }
            }
            else if(line.charAt(0) == 'v' && line.charAt(1) == 't') {
                String numstr[] = line.split("vt ");
                String[] coordvals = numstr[1].split(" ");
                for(String ch : coordvals) {
                    tuvlist.add(Float.parseFloat(ch));
                }
            }
            else if(line.charAt(0) == 'f' && line.charAt(1) == ' ') {
                String numstr[] = line.split("f ");
                String[] coordvals = numstr[1].split(" ");
                int[] vindex = new int[3];
                int[] uvindex = new int[3];
                int[] vnindex = new int[3];
                if(coordvals.length == 3) {
                    for (int n = 0; n < 3; n++) {
                        String subnums[] = coordvals[n].split("/");
                        vindex[n] = Integer.parseInt(subnums[0]);
                        uvindex[n] = Integer.parseInt(subnums[1]);
                        vnindex[n] = Integer.parseInt(subnums[2]);
                    }
                    for (int n = 0; n < 3; n++) {
                        vertexindices.add(vindex[n]);
                        uvindices.add(uvindex[n]);
                        normalsindices.add(vnindex[n]);
                    }
                }
            }
        }
        if(vertexindices.size() > 1) {
            for(int n = 0; n < vertexindices.size(); n++) {
                int vertindex = (vertexindices.get(n) - 1)*3 + 1;
                int normindex = normalsindices.get(n);
                int uvindex = uvindices.get(n);
                vertexlist.add(tvertexlist.get(vertindex-1));
                vertexlist.add(tvertexlist.get(vertindex));
                vertexlist.add(tvertexlist.get(vertindex+1));
                normalslist.add(tnormalslist.get(normindex-1));
                uvlist.add(tuvlist.get(uvindex-1));
            }
        }
        else {
            for(int n = 0; n < tvertexlist.size(); n++) {
                vertexlist.add(tvertexlist.get(n));
            }
        }
        is.close();
        updateVertexBuffer();
    }

    //Imports and loads shaders from raw resource file, currently only supports
    //one of two kinds of set shaders (vertex, fragment).
    public void importShaderFromFile (int id) throws IOException {
        InputStream is = context.getResources().openRawResource(id);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line);
        }
        switch(id) {
            case R.raw.vshader:
                vertexShaderCode = total.toString();
                break;
            case R.raw.fshader:
                fragmentShaderCode = total.toString();
                break;
        }
        is.close();
        updateShaderObjects();
    }

    //Attaches and applies most recent/updated vertex and fragment shader code (GLSL), and links
    //an updated gl program.
    public void updateShaderObjects () {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    //Updates vertex buffer from vertexlist, pushes all size and vertex data from list
    //to buffer object, and then to an allocated ByteBuffer.
    public void updateVertexBuffer () {
        vsize = vertexlist.size();
        vertexCount = vsize / COORDS_PER_VERTEX;
        ByteBuffer bb = ByteBuffer.allocateDirect(vsize * BYTESIZE);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(genVertexBuffer());
        vertexBuffer.position(0);
    }

    //Append a vertex to vertexlist
    public void appendVertex3f (float xcoord, float ycoord, float zcoord) {
        vertexlist.add(xcoord);
        vertexlist.add(ycoord);
        vertexlist.add(zcoord);
    }

    //Take a vertex data array and load it into the dynamic vertexlist
    public void loadVertexList (float[] vertexbuf) {
        for(int n = 0; n < vertexbuf.length; n++)
            vertexlist.add(vertexbuf[n]);
    }

    //Generates vertex buffer/array from vertexlist, returns new array
    public float[] genVertexBuffer() {
        float vertexbuf[] = new float[vsize];
        for(int n = 0; n < vsize; n++)
            vertexbuf[n] = vertexlist.get(n);
        return vertexbuf;
    }

    //Compiles and loads shader from type and provided GLSL code in string format
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
