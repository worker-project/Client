package com.workerai.client.modules.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.keybinds.WorkerBind;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.Optional;

public class ModuleConfigWriter extends TypeAdapter<WorkerBind> {
    @Override
    public void write(JsonWriter writer, WorkerBind value) throws IOException {
        writer.beginObject();
        writer.name("name").value(value.getKeyDescription());
        writer.name("val").value(value.getKeyCode());
        writer.endObject();
    }

    @Override
    public WorkerBind read(JsonReader reader) throws IOException {
        reader.beginObject();
        String fieldname = null;
        String name = "";
        int value = 0;

        while (reader.hasNext()) {
            JsonToken token = reader.peek();

            if (token.equals(JsonToken.NAME)) {
                fieldname = reader.nextName();
            }

            if ("name".equals(fieldname)) {
                token = reader.peek();
                name = (reader.nextString());
            }
            if ("val".equals(fieldname)) {
                token = reader.peek();
                value = (reader.nextInt());
            }
        }

        reader.endObject();
        String finalName = name;
        Optional<WorkerBind> t = WorkerClient.getInstance().getHandlersManager().getKeyboardHandler().getWorkerKeybinds().stream().filter(workerBind -> workerBind.getKeyDescription().equals(finalName)).findFirst();
        if(value != 0) {
            if(t.isPresent()) {
                t.get().setKeyCode(value);
            }
        }

        return t.orElse(null);
    }
}
