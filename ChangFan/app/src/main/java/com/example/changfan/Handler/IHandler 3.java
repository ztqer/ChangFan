package com.example.changfan.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IHandler {
	boolean HandleMessage(InputStream is, OutputStream os, byte[] buffer)throws IOException;
}
