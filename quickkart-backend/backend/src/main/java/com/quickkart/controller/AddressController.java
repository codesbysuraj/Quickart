package com.quickkart.controller;

import com.quickkart.entity.Address;
import com.quickkart.entity.User;
import com.quickkart.repository.AddressRepository;
import com.quickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/{username}/addresses")
@CrossOrigin(origins = "*")
public class AddressController {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    // GET /api/users/{username}/addresses
    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(@PathVariable String username) {
        List<Address> addresses = addressRepository.findByUserUsername(username);
        return ResponseEntity.ok(addresses);
    }

    // PUT /api/users/{username}/addresses/{addressId}/default
    // Frontend calls this endpoint to set default address
    @PutMapping("/{addressId}/default")
    public ResponseEntity<Address> setDefaultAddress(@PathVariable String username, @PathVariable Long addressId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Address> addressOpt = addressRepository.findById(addressId);
        if (userOpt.isPresent() && addressOpt.isPresent()) {
            User user = userOpt.get();
            // Set all user's addresses to not default
            List<Address> addresses = addressRepository.findByUserUsername(username);
            for (Address addr : addresses) {
                if (addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            }
            // Set selected address to default
            Address defaultAddr = addressOpt.get();
            defaultAddr.setDefault(true);
            addressRepository.save(defaultAddr);
            return ResponseEntity.ok(defaultAddr);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/users/{username}/addresses
    @PostMapping
    public ResponseEntity<Address> addAddress(@PathVariable String username, @RequestBody Address address) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        address.setUser(user);
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/users/{username}/addresses/{addressId}
    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable String username, @PathVariable Long addressId, @RequestBody Address address) {
        Optional<Address> existing = addressRepository.findById(addressId);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (existing.isPresent() && userOpt.isPresent()) {
            Address addr = existing.get();
            addr.setFullAddress(address.getFullAddress());
            addr.setPincode(address.getPincode());
            addr.setPhone(address.getPhone());
            addr.setDefault(address.isDefault());
            Address updated = addressRepository.save(addr);

            // Also update user's main pincode if changed
            User user = userOpt.get();
            if (!user.getPincode().equals(address.getPincode())) {
                user.setPincode(address.getPincode());
                userRepository.save(user);
            }

            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/users/{username}/addresses/{addressId}
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String username, @PathVariable Long addressId) {
        addressRepository.deleteById(addressId);
        return ResponseEntity.noContent().build();
    }
}
