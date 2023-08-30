package org.example.core.dto.report;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ParamsSerializer extends StdSerializer<ReportParamAudit> {

    public ParamsSerializer() {
        this(null);
    }

    protected ParamsSerializer(Class<ReportParamAudit> t) {
        super(t);
    }

    @Override
    public void serialize(ReportParamAudit value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        if (value.getType() != null) {
            gen.writeStringField(value.getType().getId(), value.getId().toString());
        }
        gen.writeFieldName("from"); gen.writeObject(value.getFrom());
        gen.writeFieldName("to"); gen.writeObject(value.getTo());
        gen.writeEndObject();


    }
}
