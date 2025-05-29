package com.paymentchain.customer.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "This model is used to return errors in RFC 7807")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StandarizedApiExceptionResponse{

    @Schema(description = "Unique uri identifier that categorizes the error", name = "type", requiredMode = Schema.RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized")
    private String type;

    @Schema(description = "Brief, human-readable message about the error", name = "title", requiredMode = Schema.RequiredMode.REQUIRED, example = "Not Authorized")
    private String title;

    @Schema(description = "Unique error code", name = "code", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "401")
    private String code;

    @Schema(description = "Longer, human-readable explanation of the error", name = "detail", requiredMode = Schema.RequiredMode.REQUIRED, example = "The user is not authorized to perform this action")
    private String detail;

    @Schema(description = "URI that identifies the specific occurrence of the error", name = "instance", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "/errors/authentication/not-authorized/01")
    private String instance;

    public StandarizedApiExceptionResponse(String type, String title, String code, String detail) {
        this.type = type;
        this.title = title;
        this.code = code;
        this.detail = detail;
    }
}
