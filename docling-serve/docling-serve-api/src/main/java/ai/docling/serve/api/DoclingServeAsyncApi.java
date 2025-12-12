package ai.docling.serve.api;

import ai.docling.serve.api.async.request.AsyncChunkDocumentRequest;
import ai.docling.serve.api.async.request.AsyncConvertDocumentRequest;
import ai.docling.serve.api.chunk.response.ChunkDocumentResponse;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.task.response.TaskStatusPollResponse;

/**
 * Defines the interface for asynchronous document conversion and chunking operations.
 *
 * <p>This API provides methods to initiate async conversions and chunking operations
 * that return immediately with a task ID. The caller can then poll the task status
 * using the Task API until completion, or use the convenience methods that handle
 * polling automatically.
 *
 * <p>The async API is useful for large documents or when you need to process multiple
 * documents concurrently without blocking.
 */
public interface DoclingServeAsyncApi {

  /**
   * Initiates an asynchronous document conversion.
   *
   * <p>This method starts a background conversion task and immediately returns a response
   * containing the task ID. Use the Task API ({@link DoclingServeTaskApi#pollTaskStatus})
   * to monitor the task status and retrieve results when complete.
   *
   * @param request the {@link AsyncConvertDocumentRequest} containing the source(s) and conversion options
   * @return a {@link TaskStatusPollResponse} containing the task ID and initial status
   */
  TaskStatusPollResponse convertSourceAsync(AsyncConvertDocumentRequest request);

  /**
   * Initiates an asynchronous document conversion and waits for completion.
   *
   * <p>This is a convenience method that combines async conversion initiation with
   * automatic polling until the task completes, fails, or times out.
   *
   * @param request the {@link AsyncConvertDocumentRequest} containing the source(s),
   *                conversion options, and polling configuration
   * @return a {@link ConvertDocumentResponse} containing the converted document
   * @throws RuntimeException if the conversion fails, times out, or is interrupted
   */
  ConvertDocumentResponse convertSourceAsyncAndWait(AsyncConvertDocumentRequest request);

  /**
   * Initiates an asynchronous document chunking with hierarchical chunker.
   *
   * <p>This method starts a background chunking task using the hierarchical chunker
   * and immediately returns a response containing the task ID.
   *
   * @param request the {@link AsyncChunkDocumentRequest} containing the source(s) and chunking options
   * @return a {@link TaskStatusPollResponse} containing the task ID and initial status
   */
  TaskStatusPollResponse chunkSourceWithHierarchicalChunkerAsync(AsyncChunkDocumentRequest request);

  /**
   * Initiates an asynchronous document chunking with hierarchical chunker and waits for completion.
   *
   * <p>This is a convenience method that combines async chunking initiation with
   * automatic polling until the task completes, fails, or times out.
   *
   * @param request the {@link AsyncChunkDocumentRequest} containing the source(s),
   *                chunking options, and polling configuration
   * @return a {@link ChunkDocumentResponse} containing the chunked document
   * @throws RuntimeException if the chunking fails, times out, or is interrupted
   */
  ChunkDocumentResponse chunkSourceWithHierarchicalChunkerAsyncAndWait(AsyncChunkDocumentRequest request);

  /**
   * Initiates an asynchronous document chunking with hybrid chunker.
   *
   * <p>This method starts a background chunking task using the hybrid chunker
   * and immediately returns a response containing the task ID.
   *
   * @param request the {@link AsyncChunkDocumentRequest} containing the source(s) and chunking options
   * @return a {@link TaskStatusPollResponse} containing the task ID and initial status
   */
  TaskStatusPollResponse chunkSourceWithHybridChunkerAsync(AsyncChunkDocumentRequest request);

  /**
   * Initiates an asynchronous document chunking with hybrid chunker and waits for completion.
   *
   * <p>This is a convenience method that combines async chunking initiation with
   * automatic polling until the task completes, fails, or times out.
   *
   * @param request the {@link AsyncChunkDocumentRequest} containing the source(s),
   *                chunking options, and polling configuration
   * @return a {@link ChunkDocumentResponse} containing the chunked document
   * @throws RuntimeException if the chunking fails, times out, or is interrupted
   */
  ChunkDocumentResponse chunkSourceWithHybridChunkerAsyncAndWait(AsyncChunkDocumentRequest request);
}
