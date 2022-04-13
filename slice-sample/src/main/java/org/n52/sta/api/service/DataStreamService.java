package org.n52.sta.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import org.n52.sta.api.domain.DataStreamAggregate;
import org.n52.sta.api.serializer.Serializer;
import org.springframework.stereotype.Service;

@Service
public class DataStreamService {

    // Paging, metadata, ...
    public <S> Stream<S> getCollection(Serializer<DataStreamAggregate, S> serializer) {
        ArrayList<DataStreamAggregate> list = new ArrayList<>();


        Collections.addAll(list, new DataStreamAggregate());


        return list.stream().map(serializer::serialize);
    }
}
