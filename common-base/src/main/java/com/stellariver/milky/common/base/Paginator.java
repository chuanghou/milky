package com.stellariver.milky.common.base;

import lombok.Data;

@Data
public class Paginator {

    private long items;

    private long page;

    private long itemsPerPage;

    private long pages;

}
