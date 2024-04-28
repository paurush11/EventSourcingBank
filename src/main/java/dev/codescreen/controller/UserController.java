package dev.codescreen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.codescreen.dto.Amount;
import dev.codescreen.dto.LoadResponse;
import dev.codescreen.dto.ResponseCode;
import dev.codescreen.dto.UserDTO;
import dev.codescreen.model.Event;
import dev.codescreen.model.LoadEvent;
import dev.codescreen.model.User;
import dev.codescreen.repository.EventRepository;
import dev.codescreen.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TransactionCommandService transactionCommandService;
    @Autowired
    private BalanceProjectionService balanceProjectionService;

    public LoadEvent createFirstLoadEvent(String userId, Amount amount) {
        String messageId = UUID.randomUUID().toString();
        LoadEvent event = new LoadEvent(
                messageId,
                userId,
                LocalDateTime.now(),
                "LOAD",
                amount,
                null,  // Response code will be set in the service
                false  // approved status will be determined in the service
        );
        eventService.saveEvent(event);
        return event;
    }

    @PostMapping
    public DeferredResult<ResponseEntity<User>> createUser(@RequestBody UserDTO userDto) {
        DeferredResult<ResponseEntity<User>> deferredResult = new DeferredResult<>();
        try {
            User user = new User();// Conversion from DTO to entity should happen here
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setBalance(userDto.getBalance());// Consider encryption here
            User createdUser = userService.createUser(user);
            LoadEvent event = createFirstLoadEvent(user.getId(), user.getBalance());
            transactionCommandService.sendEvent(event, "loadQueue").thenAccept(aVoid -> {
                deferredResult.setResult(new ResponseEntity<>(createdUser, HttpStatus.CREATED));
                event.setResponseCode(ResponseCode.APPROVED);
                event.setSuccess(true);
                System.out.println(event);
                eventService.saveEvent(event);
            }).exceptionally(ex -> {
                event.setResponseCode(ResponseCode.DECLINED);
                event.setSuccess(false);
                eventService.saveEvent(event);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            });

        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return deferredResult;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") String id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such User found");
        }
    }

    @GetMapping("/replayEvents/{id}")
    public ResponseEntity<String> replayEvents(@PathVariable("id") String id) {
        try {
            List<Event> event = eventService.replayEvents(id);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            Collections.reverse(event);
            return ResponseEntity.ok(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to replay events: " + e.getMessage());
        }
    }

    @GetMapping("/reCalculateBalance/{id}")
    public ResponseEntity<User> reCalculateBalanceByReplayingEvents(@PathVariable("id") String id) {
        try {
            Amount finalAmt = eventService.replayEventsAndGetNewBalance(id);
            User existingUser = userService.getUserById(id);
            existingUser.setBalance(finalAmt);
            return new ResponseEntity<User>(existingUser, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to replay events: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody UserDTO userDto) {
        try {
            User existingUser = userService.getUserById(id);
            existingUser.setUsername(userDto.getUsername());
            existingUser.setEmail(userDto.getEmail());
            User updatedUser = userService.updateUser(existingUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        try {
            User existingUser = userService.getUserById(id);
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
        }

    }

}
