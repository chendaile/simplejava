package app.DTO;

/**
 * 通用 API 响应类
 * 
 * @param <T> 数据类型
 */
public class ApiResponse<T> {
    public int code;
    public String message;
    public T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应 (code = 0)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    /**
     * 成功响应，无数据
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(0, message, null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
