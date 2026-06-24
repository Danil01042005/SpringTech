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
        return new FeignException.NotFound("не найдено", request, null, null);
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
        return new FeignException.InternalServerError("медицина недоступна", request, null, null);
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
        return new FeignException.Forbidden("доступ запрещён", request, null, null);
    }

    public static FeignException serviceUnavailable(String method, String path) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                path,
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return new FeignException(503, "сервис недоступен", request, null, null) {
        };
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
        return new FeignException(-1, "таймаут", request, null, null) {
        };
    }
}
