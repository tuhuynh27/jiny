package com.jinyframework.core;

import java.io.IOException;

public interface RequestPipelineBase {
    void run() throws IOException;
}
