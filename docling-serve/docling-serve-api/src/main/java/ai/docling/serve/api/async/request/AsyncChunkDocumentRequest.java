package ai.docling.serve.api.async.request;

import java.time.Duration;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import ai.docling.serve.api.chunk.request.options.ChunkerOptions;
import ai.docling.serve.api.convert.request.options.ConvertDocumentOptions;
import ai.docling.serve.api.convert.request.source.Source;
import ai.docling.serve.api.convert.request.target.Target;

/**
 * Represents a request for asynchronous document chunking.
 *
 * <p>This class extends the standard chunking request with additional configuration
 * options for async operation polling, including poll interval and timeout settings.
 *
 * <p>The polling-related fields ({@code pollInterval} and {@code timeout}) are marked
 * with {@link JsonIgnore} as they are used client-side only and should not be sent
 * to the server.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@tools.jackson.databind.annotation.JsonDeserialize(builder = AsyncChunkDocumentRequest.Builder.class)
@lombok.extern.jackson.Jacksonized
@lombok.Builder(toBuilder = true)
@lombok.Getter
@lombok.ToString
public class AsyncChunkDocumentRequest {

  /**
   * Default polling interval for checking task status.
   */
  public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(2);

  /**
   * Default timeout for async operations.
   */
  public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

  @JsonProperty("sources")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  @lombok.Singular
  private List<Source> sources;

  @JsonProperty("convert_options")
  @lombok.NonNull
  @lombok.Builder.Default
  private ConvertDocumentOptions options = ConvertDocumentOptions.builder().build();

  @JsonProperty("target")
  @Nullable
  private Target target;

  @JsonProperty("include_converted_doc")
  private boolean includeConvertedDoc;

  @JsonProperty("chunking_options")
  @Nullable
  private ChunkerOptions chunkingOptions;

  /**
   * The interval between status polling attempts.
   * Only used by the client for the "and wait" methods.
   */
  @JsonIgnore
  @lombok.NonNull
  @lombok.Builder.Default
  private Duration pollInterval = DEFAULT_POLL_INTERVAL;

  /**
   * The maximum time to wait for the async operation to complete.
   * Only used by the client for the "and wait" methods.
   */
  @JsonIgnore
  @lombok.NonNull
  @lombok.Builder.Default
  private Duration timeout = DEFAULT_TIMEOUT;

  @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
  public static class Builder { }
}
