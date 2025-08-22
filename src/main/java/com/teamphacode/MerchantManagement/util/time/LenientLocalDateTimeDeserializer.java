package com.teamphacode.MerchantManagement.util.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter DMY_HMS = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
    private static final DateTimeFormatter DMY_HM  = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final DateTimeFormatter DMY     = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String s = p.getText();
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        try { return LocalDateTime.parse(s, DMY_HMS); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, DMY_HM);  } catch (Exception ignored) {}
        try { return LocalDate.parse(s, DMY).atStartOfDay(); } catch (Exception ignored) {}
        throw new JsonParseException(p, "Ngày/giờ không đúng định dạng. Hỗ trợ: dd/MM/yyyy, dd/MM/yyyy HH:mm[:ss]");
    }
}
