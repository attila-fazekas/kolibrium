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

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// ============================================================================
// Password Utility
// ============================================================================
private object PasswordUtil {
    fun hash(password: Password): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.value.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun verify(password: Password, hash: String): Boolean {
        return hash(password) == hash
    }
}

// ============================================================================
// JWT Configuration
// ============================================================================
private object JwtConfig {
    private const val SECRET = "vinyl-store-secret-key-for-testing"
    private const val ISSUER = "vinyl-store-api"
    private const val AUDIENCE = "vinyl-store-users"
    private const val VALIDITY_MS = 10 * 60 * 1000L // 10 minutes

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(SECRET))
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: Int, email: Email, role: Role): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("email", email.value)
            .withClaim("role", role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }
}

// ============================================================================
// Data Models
// ============================================================================
@Serializable
@JvmInline
private value class Email(val value: String)

@Serializable
@JvmInline
private value class Password(val value: String)

@Serializable
private data class User(
    val id: Int,
    val email: Email,
    val passwordHash: String,
    val role: Role,
    val isActive: Boolean,
    val createdAt: Long,
)

@Serializable
private data class Address(
    val id: Int,
    val userId: Int,
    val type: String, // SHIPPING, BILLING
    val fullName: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean,
)

@Serializable
private data class Artist(
    val id: Int,
    val name: String,
)

@Serializable
private data class Genre(
    val id: Int,
    val name: String,
)

@Serializable
private data class Label(
    val id: Int,
    val name: String,
)

@Serializable
private data class Vinyl(
    val id: Int,
    val title: String,
    val artistId: Int,
    val labelId: Int,
    val year: Int,
    val conditionMedia: String,
    val conditionSleeve: String,
)

@Serializable
private data class Listing(
    val id: Int,
    val vinylId: Int,
    val status: String, // DRAFT, PUBLISHED
    val price: Double,
    val currency: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
private data class Inventory(
    val id: Int,
    val listingId: Int,
    val totalQuantity: Int,
    val reservedQuantity: Int,
) {
    val availableQuantity: Int get() = totalQuantity - reservedQuantity
}

@Serializable
private data class VinylGenre(
    val vinylId: Int,
    val genreId: Int,
)

// Request/Response DTOs
@Serializable
private data class RegisterRequest(
    val email: Email,
    val password: Password,
)

@Serializable
private data class LoginRequest(
    val email: Email,
    val password: Password,
)

@Serializable
private data class LoginResponse(
    val token: String,
    val user: UserResponse,
)

@Serializable
private data class UserResponse(
    val id: Int,
    val email: Email,
    val role: Role,
    val isActive: Boolean,
)

@Serializable
private data class CreateListingRequest(
    val vinylId: Int,
    val price: Double,
    val currency: String = "USD",
    val initialStock: Int,
)

@Serializable
private data class UpdateListingRequest(
    val price: Double? = null,
    val status: String? = null,
)

@Serializable
private data class ListingDetailResponse(
    val listing: Listing,
    val vinyl: Vinyl,
    val artist: Artist,
    val genres: List<Genre>,
    val label: Label,
    val inventory: Inventory,
)

@Serializable
private data class CreateVinylRequest(
    val title: String,
    val artistId: Int,
    val labelId: Int,
    val year: Int,
    val conditionMedia: String,
    val conditionSleeve: String,
    val genreIds: List<Int>,
)

@Serializable
private data class CreateArtistRequest(val name: String)

@Serializable
private data class CreateGenreRequest(val name: String)

@Serializable
private data class CreateLabelRequest(val name: String)

@Serializable
private data class CreateAddressRequest(
    val type: String,
    val fullName: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean = false,
)

@Serializable
private data class UpdateInventoryRequest(
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
)

@Serializable
private data class ErrorResponse(
    val error: String,
    val message: String,
)

@Serializable
private data class HealthResponse(
    val status: String,
    val uptime: Long,
    val nextResetIn: Long,
)

@Serializable
private data class ListingsResponse(
    val listings: List<ListingDetailResponse>,
    val total: Int,
)

@Serializable
private data class ArtistsResponse(
    val artists: List<Artist>,
)

@Serializable
private data class GenresResponse(
    val genres: List<Genre>,
)

@Serializable
private data class LabelsResponse(
    val labels: List<Label>,
)

@Serializable
private data class VinylsResponse(
    val vinyls: List<Vinyl>,
)

@Serializable
private data class AddressesResponse(
    val addresses: List<Address>,
)

@Serializable
private data class MessageResponse(
    val message: String,
)

private enum class Role {
    ADMIN,
    CUSTOMER
}

// ============================================================================
// Store
// ============================================================================
private class VinylStoreData {
    val users = ConcurrentHashMap<Int, User>()
    val addresses = ConcurrentHashMap<Int, Address>()
    val artists = ConcurrentHashMap<Int, Artist>()
    val genres = ConcurrentHashMap<Int, Genre>()
    val labels = ConcurrentHashMap<Int, Label>()
    val vinyls = ConcurrentHashMap<Int, Vinyl>()
    val vinylGenres = ConcurrentHashMap<String, VinylGenre>()
    val listings = ConcurrentHashMap<Int, Listing>()
    val inventory = ConcurrentHashMap<Int, Inventory>()

    val userIdCounter = AtomicInteger(1)
    val addressIdCounter = AtomicInteger(1)
    val artistIdCounter = AtomicInteger(1)
    val genreIdCounter = AtomicInteger(1)
    val labelIdCounter = AtomicInteger(1)
    val vinylIdCounter = AtomicInteger(1)
    val listingIdCounter = AtomicInteger(1)
    val inventoryIdCounter = AtomicInteger(1)

    val createdAt = System.currentTimeMillis()

    init {
        bootstrap()
    }

    fun shouldReset(): Boolean {
        val oneHourInMillis = 60 * 60 * 1000
        return System.currentTimeMillis() - createdAt > oneHourInMillis
    }

    fun resetToBootstrap() {
        users.clear()
        addresses.clear()
        artists.clear()
        genres.clear()
        labels.clear()
        vinyls.clear()
        vinylGenres.clear()
        listings.clear()
        inventory.clear()

        userIdCounter.set(1)
        addressIdCounter.set(1)
        artistIdCounter.set(1)
        genreIdCounter.set(1)
        labelIdCounter.set(1)
        vinylIdCounter.set(1)
        listingIdCounter.set(1)
        inventoryIdCounter.set(1)

        bootstrap()
    }

    private fun bootstrap() {
        // Create admin user
        createUser(Email("admin@vinylstore.com"), Password("admin123"), Role.ADMIN)

        // Create artists
        val donatoDozzy = createArtist("Donato Dozzy")
        val dominikEulberg = createArtist("Dominik Eulberg")
        val extrawelt = createArtist("Extrawelt")
        val scuba = createArtist("Scuba")
        val trentemoller = createArtist("Trentemøller")

        // Create genres
        val ambient = createGenre("Ambient")
        val dubstep = createGenre("Dubstep")
        val electro = createGenre("Electro")
        val electronic = createGenre("Electronic")
        val minimal = createGenre("Minimal")
        val techHouse = createGenre("Tech House")
        val techno = createGenre("Techno")

        // Create labels
        val furtherRecords = createLabel("Further Records")
        val k7Records = createLabel("!K7 Records")
        val cocoonRecordings = createLabel("Cocoon Recordings")
        val hotflushRecordings = createLabel("Hotflush Recordings")
        val pokerFlatRecordings = createLabel("Poker Flat Recordings")

        // Create vinyls
        val k = createVinyl("K", donatoDozzy.id, furtherRecords.id, 2014, "VG+", "VG+")
        linkVinylGenre(k.id, ambient.id)
        linkVinylGenre(k.id, techno.id)

        val avichrom = createVinyl("Avichrom", dominikEulberg.id, k7Records.id, 2022, "M", "M")
        linkVinylGenre(avichrom.id, electronic.id)

        val dystortion = createVinyl("Dystortion", extrawelt.id, cocoonRecordings.id, 2025, "M", "M")
        linkVinylGenre(dystortion.id, techno.id)
        linkVinylGenre(dystortion.id, electro.id)
        linkVinylGenre(dystortion.id, techHouse.id)

        val triangulation = createVinyl("Triangulation", scuba.id, hotflushRecordings.id, 2010, "VG", "VG")
        linkVinylGenre(triangulation.id, techno.id)
        linkVinylGenre(triangulation.id, dubstep.id)

        val theLastResort = createVinyl("The Last Resort", trentemoller.id, pokerFlatRecordings.id, 2018, "NM", "M")
        linkVinylGenre(theLastResort.id, techHouse.id)
        linkVinylGenre(theLastResort.id, ambient.id)
        linkVinylGenre(theLastResort.id, minimal.id)
        linkVinylGenre(theLastResort.id, techHouse.id)

        // Create published listings with inventory
        createListing(k.id, 99.99, "EUR", 15)
        createListing(avichrom.id, 31.99, "EUR", 20)
        createListing(dystortion.id, 33.99, "EUR", 10)
        createListing(triangulation.id, 22.99, "EUR", 12)
        createListing(theLastResort.id, 26.99, "EUR", 8)
    }

    fun createUser(email: Email, password: Password, role: Role): User {
        val id = userIdCounter.getAndIncrement()
        val user = User(
            id = id,
            email = email,
            passwordHash = PasswordUtil.hash(password),
            role = role,
            isActive = true,
            createdAt = System.currentTimeMillis(),
        )
        users[id] = user
        return user
    }

    fun getUserByEmail(email: Email): User? = users.values.find { it.email == email }

    fun getUserById(id: Int): User? = users[id]

    fun createAddress(
        userId: Int,
        type: String,
        fullName: String,
        street: String,
        city: String,
        postalCode: String,
        country: String,
        isDefault: Boolean,
    ): Address {
        if (isDefault) {
            addresses.values.filter { it.userId == userId && it.type == type }.forEach {
                addresses[it.id] = it.copy(isDefault = false)
            }
        }
        val id = addressIdCounter.getAndIncrement()
        val address = Address(id, userId, type, fullName, street, city, postalCode, country, isDefault)
        addresses[id] = address
        return address
    }

    fun getAddressesByUserId(userId: Int): List<Address> =
        addresses.values.filter { it.userId == userId }.sortedBy { it.id }

    fun createArtist(name: String): Artist {
        val id = artistIdCounter.getAndIncrement()
        val artist = Artist(id, name)
        artists[id] = artist
        return artist
    }

    fun createGenre(name: String): Genre {
        val id = genreIdCounter.getAndIncrement()
        val genre = Genre(id, name)
        genres[id] = genre
        return genre
    }

    fun createLabel(name: String): Label {
        val id = labelIdCounter.getAndIncrement()
        val label = Label(id, name)
        labels[id] = label
        return label
    }

    fun createVinyl(
        title: String,
        artistId: Int,
        labelId: Int,
        year: Int,
        conditionMedia: String,
        conditionSleeve: String,
    ): Vinyl {
        val id = vinylIdCounter.getAndIncrement()
        val vinyl = Vinyl(id, title, artistId, labelId, year, conditionMedia, conditionSleeve)
        vinyls[id] = vinyl
        return vinyl
    }

    fun linkVinylGenre(vinylId: Int, genreId: Int) {
        vinylGenres["$vinylId-$genreId"] = VinylGenre(vinylId, genreId)
    }

    fun getGenresForVinyl(vinylId: Int): List<Genre> {
        val genreIds = vinylGenres.values.filter { it.vinylId == vinylId }.map { it.genreId }
        return genreIds.mapNotNull { genres[it] }
    }

    fun createListing(vinylId: Int, price: Double, currency: String, initialStock: Int): Listing {
        val id = listingIdCounter.getAndIncrement()
        val now = System.currentTimeMillis()
        val listing = Listing(id, vinylId, "PUBLISHED", price, currency, now, now)
        listings[id] = listing

        val inventoryId = inventoryIdCounter.getAndIncrement()
        inventory[id] = Inventory(inventoryId, id, initialStock, 0)
        return listing
    }

    fun updateListing(id: Int, price: Double?, status: String?): Listing? {
        val existing = listings[id] ?: return null
        val updated = existing.copy(
            price = price ?: existing.price,
            status = status ?: existing.status,
            updatedAt = System.currentTimeMillis(),
        )
        listings[id] = updated
        return updated
    }

    fun getListingById(id: Int): Listing? = listings[id]

    fun getAllPublishedListings(): List<Listing> =
        listings.values.filter { it.status == "PUBLISHED" }.sortedBy { it.id }

    fun getInventoryByListingId(listingId: Int): Inventory? = inventory[listingId]

    fun updateInventory(listingId: Int, totalQuantity: Int?, reservedQuantity: Int?): Inventory? {
        val existing = inventory[listingId] ?: return null
        val updated = existing.copy(
            totalQuantity = totalQuantity ?: existing.totalQuantity,
            reservedQuantity = reservedQuantity ?: existing.reservedQuantity,
        )
        inventory[listingId] = updated
        return updated
    }
}

// ============================================================================
// User Principal
// ============================================================================
private data class UserPrincipal(
    val userId: Int,
    val email: String,
    val role: String,
) : Principal

// ============================================================================
// Authorization Helper
// ============================================================================
// TODO use Role type instead of String
private suspend fun ApplicationCall.requireRole(vararg allowedRoles: String) {
    val principal = principal<UserPrincipal>()
        ?: run {
            respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized", "Not authenticated"))
            return
        }

    if (principal.role !in allowedRoles) {
        respond(HttpStatusCode.Forbidden, ErrorResponse("Forbidden", "Insufficient permissions"))
        return
    }
}

// ============================================================================
// Application Module
// ============================================================================
private fun Application.vinylStoreModule(store: VinylStoreData = VinylStoreData()) {
    install(OpenApi) {
        outputFormat = OutputFormat.JSON
        info {
            title = "Vinyl Store API"
            version = "0.1.0"
            description = "API description"
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        security {
            securityScheme("UserAuth") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
            }
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            },
        )
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val email = credential.payload.getClaim("email").asString()
                val role = credential.payload.getClaim("role").asString()
                UserPrincipal(userId, email, role)
            }
        }
    }

    // Auto-reset checker
    intercept(ApplicationCallPipeline.Monitoring) {
        if (store.shouldReset()) {
            store.resetToBootstrap()
        }
    }

    routing {
        route("api.json") {
            openApi()
        }

        route("swagger") {
            swaggerUI(openApiUrl = "/api.json") {
                // Add configuration for this Swagger UI "instance" here.
            }
        }

        // Health check
        get("/health") {
            val uptime = System.currentTimeMillis() - store.createdAt
            val oneHour = 60 * 60 * 1000
            val nextReset = oneHour - uptime
            call.respond(HealthResponse("ok", uptime, nextReset))
        }

        // Auth routes
        route("/api/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()

                if (request.email.value.isBlank() || !request.email.value.contains("@")) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Invalid email"))
                    return@post
                }

                if (request.password.value.length < 8) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Password must be at least 8 characters")
                    )
                    return@post
                }

                if (store.getUserByEmail(request.email) != null) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("conflict", "Email already exists"))
                    return@post
                }

                val user = store.createUser(request.email, request.password, Role.CUSTOMER)
                val token = JwtConfig.generateToken(user.id, user.email, user.role)

                call.respond(
                    HttpStatusCode.Created,
                    LoginResponse(
                        token = token,
                        user = UserResponse(user.id, user.email, user.role, user.isActive),
                    ),
                )
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val user = store.getUserByEmail(request.email)

                if (user == null || !PasswordUtil.verify(request.password, user.passwordHash)) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", "Invalid credentials"))
                    return@post
                }

                if (!user.isActive) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("forbidden", "Account is not active"))
                    return@post
                }

                val token = JwtConfig.generateToken(user.id, user.email, user.role)
                call.respond(LoginResponse(token, UserResponse(user.id, user.email, user.role, user.isActive)))
            }

            authenticate("auth-jwt") {
                get("/me", {
                    securitySchemeNames("UserAuth")
                }) {
                    val principal = call.principal<UserPrincipal>()!!
                    val user = store.getUserById(principal.userId)!!
                    call.respond(UserResponse(user.id, user.email, user.role, user.isActive))
                }
            }
        }

        // Listings routes
        route("/api/listings") {
            get {
                val allListings = store.getAllPublishedListings()
                val details = allListings.mapNotNull { listing ->
                    val vinyl = store.vinyls[listing.vinylId] ?: return@mapNotNull null
                    val artist = store.artists[vinyl.artistId] ?: return@mapNotNull null
                    val label = store.labels[vinyl.labelId] ?: return@mapNotNull null
                    val genres = store.getGenresForVinyl(vinyl.id)
                    val inv = store.getInventoryByListingId(listing.id) ?: return@mapNotNull null

                    if (inv.availableQuantity > 0) {
                        ListingDetailResponse(listing, vinyl, artist, genres, label, inv)
                    } else null
                }
                call.respond(ListingsResponse(listings = details, total = details.size))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid listing ID"))
                    return@get
                }

                val listing = store.getListingById(id)
                if (listing == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Listing not found"))
                    return@get
                }

                val vinyl = store.vinyls[listing.vinylId]!!
                val artist = store.artists[vinyl.artistId]!!
                val label = store.labels[vinyl.labelId]!!
                val genres = store.getGenresForVinyl(vinyl.id)
                val inv = store.getInventoryByListingId(listing.id)!!

                call.respond(ListingDetailResponse(listing, vinyl, artist, genres, label, inv))
            }

            authenticate("auth-jwt") {
                post({
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")

                    val request = call.receive<CreateListingRequest>()

                    if (request.price <= 0) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("validation_error", "Price must be positive")
                        )
                        return@post
                    }

                    if (store.vinyls[request.vinylId] == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Vinyl not found"))
                        return@post
                    }

                    val listing =
                        store.createListing(request.vinylId, request.price, request.currency, request.initialStock)
                    call.respond(HttpStatusCode.Created, listing)
                }

                put("/{id}", {
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")

                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid listing ID"))
                        return@put
                    }

                    val request = call.receive<UpdateListingRequest>()
                    val updated = store.updateListing(id, request.price, request.status)

                    if (updated == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Listing not found"))
                    } else {
                        call.respond(updated)
                    }
                }
            }
        }

        // Inventory routes
        authenticate("auth-jwt") {
            route("/api/inventory") {
                get("/{listingId}", {
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")

                    val listingId = call.parameters["listingId"]?.toIntOrNull()
                    if (listingId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid listing ID"))
                        return@get
                    }

                    val inv = store.getInventoryByListingId(listingId)
                    if (inv == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Inventory not found"))
                    } else {
                        call.respond(inv)
                    }
                }

                put("/{listingId}", {
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")

                    val listingId = call.parameters["listingId"]?.toIntOrNull()
                    if (listingId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid listing ID"))
                        return@put
                    }

                    val request = call.receive<UpdateInventoryRequest>()
                    val updated = store.updateInventory(listingId, request.totalQuantity, request.reservedQuantity)

                    if (updated == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Inventory not found"))
                    } else {
                        call.respond(updated)
                    }
                }
            }
        }

        // Artists routes
        authenticate("auth-jwt") {
            route("/api/artists") {
                get({
                    securitySchemeNames("UserAuth")
                }) {
                    call.respond(mapOf("artists" to store.artists.values.sortedBy { it.id }))
                }

                post({
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")
                    val request = call.receive<CreateArtistRequest>()
                    val artist = store.createArtist(request.name)
                    call.respond(HttpStatusCode.Created, artist)
                }
            }
        }

        // Genres routes
        authenticate("auth-jwt") {
            route("/api/genres") {
                get({
                    securitySchemeNames("UserAuth")
                }) {
                    call.respond(mapOf("genres" to store.genres.values.sortedBy { it.id }))
                }

                post({
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")
                    val request = call.receive<CreateGenreRequest>()
                    val genre = store.createGenre(request.name)
                    call.respond(HttpStatusCode.Created, genre)
                }
            }
        }

        // Labels routes
        authenticate("auth-jwt") {
            route("/api/labels") {
                get({
                    securitySchemeNames("UserAuth")
                }) {
                    call.respond(mapOf("labels" to store.labels.values.sortedBy { it.id }))
                }

                post({
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")
                    val request = call.receive<CreateLabelRequest>()
                    val label = store.createLabel(request.name)
                    call.respond(HttpStatusCode.Created, label)
                }
            }
        }

        // Vinyls routes
        authenticate("auth-jwt") {
            route("/api/vinyls") {
                get({
                    securitySchemeNames("UserAuth")
                }) {
                    call.respond(mapOf("vinyls" to store.vinyls.values.sortedBy { it.id }))
                }

                post({
                    securitySchemeNames("UserAuth")
                }) {
                    call.requireRole("ADMIN", "STAFF")
                    val request = call.receive<CreateVinylRequest>()

                    val vinyl = store.createVinyl(
                        request.title,
                        request.artistId,
                        request.labelId,
                        request.year,
                        request.conditionMedia,
                        request.conditionSleeve,
                    )

                    request.genreIds.forEach { genreId ->
                        store.linkVinylGenre(vinyl.id, genreId)
                    }

                    call.respond(HttpStatusCode.Created, vinyl)
                }
            }
        }

        // User addresses
        authenticate("auth-jwt") {
            route("/api/users/me/addresses") {
                get({
                    securitySchemeNames("UserAuth")
                }) {
                    val principal = call.principal<UserPrincipal>()!!
                    val addresses = store.getAddressesByUserId(principal.userId)
                    call.respond(mapOf("addresses" to addresses))
                }

                post({
                    securitySchemeNames("UserAuth")
                }) {
                    val principal = call.principal<UserPrincipal>()!!
                    val request = call.receive<CreateAddressRequest>()

                    val address = store.createAddress(
                        principal.userId,
                        request.type,
                        request.fullName,
                        request.street,
                        request.city,
                        request.postalCode,
                        request.country,
                        request.isDefault,
                    )

                    call.respond(HttpStatusCode.Created, address)
                }
            }
        }

        // Admin reset
        authenticate("auth-jwt") {
            post("/admin/reset", {
                securitySchemeNames("UserAuth")
            }) {
                call.requireRole("ADMIN")
                store.resetToBootstrap()
                call.respond(mapOf("message" to "Data reset to bootstrap state"))
            }
        }
    }
}

private fun startVinylStoreServer(
    port: Int = 8080,
    wait: Boolean = false,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> =
    embeddedServer(Netty, port = port) {
        vinylStoreModule()
    }.start(wait = wait)

fun main() {
    startVinylStoreServer(port = 8080, wait = true)
    println("Vinyl Store API running on http://localhost:8080")
    println("Admin credentials: admin@vinylstore.com / admin123")
    println("Try: http://localhost:8080/api/listings")
}
