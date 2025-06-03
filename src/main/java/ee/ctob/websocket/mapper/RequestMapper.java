package ee.ctob.websocket.mapper;

import ee.ctob.data.Bet;
import ee.ctob.websocket.data.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "number", target = "number")
    @Mapping(source = "amount", target = "amount")
    Bet toBet(Request request);
}
