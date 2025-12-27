/*
 * Copyright 2023-2025 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.kolibrium.examples.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.json.Json

@Serializable
private data class Vinyl(
    val id: Int,
    val artist: String,
    val album: String,
    val year: Int,
    val genre: String,
    val price: Double,
    val stock: Int,
)

@Serializable
private data class CreateVinylRequest(
    val artist: String,
    val album: String,
    val year: Int,
    val genre: String,
    val price: Double,
    val stock: Int = 0,
)

@Serializable
private data class UpdateVinylRequest(
    val artist: String? = null,
    val album: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
)

@Serializable
private data class VinylList(
    val vinyls: List<Vinyl>,
    val total: Int,
)

@Serializable
private data class ErrorResponse(
    val error: String,
    val message: String,
)

private class VinylStore {
    private val vinyls = ConcurrentHashMap<Int, Vinyl>()
    private val idCounter = AtomicInteger(1)

    init {
        // Seed with some initial data
        create(CreateVinylRequest("Pink Floyd", "The Dark Side of the Moon", 1973, "Progressive Rock", 29.99, 15))
        create(CreateVinylRequest("The Beatles", "Abbey Road", 1969, "Rock", 24.99, 20))
        create(CreateVinylRequest("Miles Davis", "Kind of Blue", 1959, "Jazz", 27.99, 10))
        create(CreateVinylRequest("Nirvana", "Nevermind", 1991, "Grunge", 22.99, 12))
        create(CreateVinylRequest("Fleetwood Mac", "Rumours", 1977, "Rock", 26.99, 8))
    }

    fun create(request: CreateVinylRequest): Vinyl {
        val id = idCounter.getAndIncrement()
        val vinyl =
            Vinyl(
                id = id,
                artist = request.artist,
                album = request.album,
                year = request.year,
                genre = request.genre,
                price = request.price,
                stock = request.stock,
            )
        vinyls[id] = vinyl
        return vinyl
    }

    fun getAll(): List<Vinyl> = vinyls.values.toList().sortedBy { it.id }

    fun getById(id: Int): Vinyl? = vinyls[id]

    fun update(
        id: Int,
        request: UpdateVinylRequest,
    ): Vinyl? {
        val existing = vinyls[id] ?: return null
        val updated =
            existing.copy(
                artist = request.artist ?: existing.artist,
                album = request.album ?: existing.album,
                year = request.year ?: existing.year,
                genre = request.genre ?: existing.genre,
                price = request.price ?: existing.price,
                stock = request.stock ?: existing.stock,
            )
        vinyls[id] = updated
        return updated
    }

    fun delete(id: Int): Boolean = vinyls.remove(id) != null

    fun findByGenre(genre: String): List<Vinyl> = vinyls.values.filter { it.genre.equals(genre, ignoreCase = true) }.sortedBy { it.id }

    fun findByArtist(artist: String): List<Vinyl> = vinyls.values.filter { it.artist.contains(artist, ignoreCase = true) }.sortedBy { it.id }

    fun clear() {
        vinyls.clear()
        idCounter.set(1)
    }
}

private fun Application.vinylStoreModule(store: VinylStore = VinylStore()) {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            },
        )
    }

    routing {
        route("/api/vinyls") {
            // GET /api/vinyls - List all vinyls with optional filters
            get {
                val genre = call.request.queryParameters["genre"]
                val artist = call.request.queryParameters["artist"]

                val vinyls =
                    when {
                        genre != null -> store.findByGenre(genre)
                        artist != null -> store.findByArtist(artist)
                        else -> store.getAll()
                    }

                call.respond(VinylList(vinyls = vinyls, total = vinyls.size))
            }

            // POST /api/vinyls - Create a new vinyl
            post {
                val request = call.receive<CreateVinylRequest>()
                val vinyl = store.create(request)
                call.respond(HttpStatusCode.Created, vinyl)
            }

            // GET /api/vinyls/{id} - Get vinyl by ID
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("bad_request", "Invalid vinyl ID"),
                    )
                    return@get
                }

                val vinyl = store.getById(id)
                if (vinyl == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("not_found", "Vinyl with ID $id not found"),
                    )
                } else {
                    call.respond(vinyl)
                }
            }

            // PUT /api/vinyls/{id} - Update vinyl
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("bad_request", "Invalid vinyl ID"),
                    )
                    return@put
                }

                val request = call.receive<UpdateVinylRequest>()
                val updated = store.update(id, request)
                if (updated == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("not_found", "Vinyl with ID $id not found"),
                    )
                } else {
                    call.respond(updated)
                }
            }

            // DELETE /api/vinyls/{id} - Delete vinyl
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("bad_request", "Invalid vinyl ID"),
                    )
                    return@delete
                }

                val deleted = store.delete(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("not_found", "Vinyl with ID $id not found"),
                    )
                }
            }
        }

        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}

private fun startVinylStoreServer(
    port: Int = 8080,
    wait: Boolean = false,
    store: VinylStore = VinylStore(),
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> =
    embeddedServer(Netty, port = port) {
        vinylStoreModule(store)
    }.start(wait = wait)

fun main() {
    startVinylStoreServer(port = 8080, wait = true)
    println("Vinyl Store API running on http://localhost:8080")
    println("Try: http://localhost:8080/api/vinyls")
}
