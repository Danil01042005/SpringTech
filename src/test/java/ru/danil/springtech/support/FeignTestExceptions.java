package ru.danil.springtech.support;

import feign.FeignException;
import feign.Request;

import java.util.Collections;

public final class FeignTestExceptions {

    private FeignTestExceptions() {
    }

    public static FeignException.NotFound notFound(String method, String path) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                path,
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return new FeignException.NotFound("not found", request, null, null);
    }

    public static FeignException.InternalServerError serverError(String method, String path) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                path,
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return new FeignException.InternalServerError("medicine unavailable", request, null, null);
    }

    public static FeignException.Forbidden forbidden(String method, String path) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                path,
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return new FeignException.Forbidden("forbidden", request, null, null);
    }

    public static FeignException timeout(String method, String path) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                path,
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return new FeignException(-1, "timeout", request, null, null) {
        };
    }
}
