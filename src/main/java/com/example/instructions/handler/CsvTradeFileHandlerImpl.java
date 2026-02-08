package com.example.instructions.handler;

import com.example.instructions.model.InputTrade;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvTradeFileHandlerImpl implements TradeFileHandlerI {

    private final CsvMapper csvMapper;

    public CsvTradeFileHandlerImpl(CsvMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    @Override public String format() { return "csv"; }

    @Override
    public boolean supports(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        return name.endsWith(".csv");
    }

    @Override
    public List<InputTrade> parse(InputStream in) throws IOException {
        CsvSchema schema = CsvSchema.builder()
                .addColumn("platform_id")
                .addColumn("account_number")
                .addColumn("security_id")
                .addColumn("trade_type")
                .addColumn("amount")
                .addColumn("timestamp")
                .setUseHeader(true)
                .build();

        MappingIterator<InputTrade> it = csvMapper.readerFor(InputTrade.class).with(schema).readValues(in);

        List<InputTrade> out = new ArrayList<>();
        while (it.hasNextValue()) out.add(it.nextValue());
        return out;
    }

}