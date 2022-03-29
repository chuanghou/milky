package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.depend.InvocationRepository;
import com.stellariver.milky.domain.support.depend.MessageRepository;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkyRepositories {

    MessageRepository messageRepository;

    InvocationRepository invocationRepository;

}
