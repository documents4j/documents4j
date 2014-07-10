package com.documents4j.job;

import java.util.concurrent.Future;

interface IConversionContext {

    Future<Boolean> asFuture();
}
