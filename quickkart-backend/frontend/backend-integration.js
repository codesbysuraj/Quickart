// ============================================================================
// QUICKKART BACKEND INTEGRATION SCRIPT
// Complete API integration for Customer and Vendor operations
// ============================================================================

const API_BASE_URL = 'http://localhost:8080/api';

// Colored console logging (NO EMOJIS)
const log = {
    success: (msg, data = null) => {
        console.log('%c[SUCCESS] ' + msg, 'color: green; font-weight: bold;', data || '');
    },
    error: (msg, data = null) => {
        console.log('%c[ERROR] ' + msg, 'color: red; font-weight: bold;', data || '');
    },
    info: (msg, data = null) => {
        console.log('%c[INFO] ' + msg, 'color: blue; font-weight: bold;', data || '');
    },
    warn: (msg, data = null) => {
        console.log('%c[WARNING] ' + msg, 'color: orange; font-weight: bold;', data || '');
    }
};

log.info('QuickKart Backend Integration Loaded', `API: ${API_BASE_URL}`);

// ============================================================================
// NOTIFICATION & DIALOG UTILITIES
// ============================================================================

const toastLevels = {
    success: 'toast--success',
    error: 'toast--error',
    warning: 'toast--warning',
    info: 'toast--info'
};

const notificationCenter = (() => {
    let container;

    const ensureContainer = () => {
        if (!container) {
            container = document.createElement('section');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    };

    const dismissToast = (toast) => {
        if (!toast) return;
        toast.classList.remove('toast--visible');
        setTimeout(() => toast.remove(), 220);
    };

    const show = ({ title, message, type = 'info', duration = 4000, dismissible = true, actions = [] } = {}) => {
        const host = ensureContainer();
        const toast = document.createElement('article');
        toast.className = `toast ${toastLevels[type] || toastLevels.info}`;

        if (title) {
            const heading = document.createElement('h4');
            heading.className = 'toast__title';
            heading.textContent = title;
            toast.appendChild(heading);
        }

        if (message) {
            const body = document.createElement('p');
            body.className = 'toast__message';
            body.textContent = message;
            toast.appendChild(body);
        }

        if (actions.length > 0) {
            const footer = document.createElement('div');
            footer.className = 'toast__actions';
            actions.forEach(action => {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = `toast__action toast__action--${action.variant || 'primary'}`;
                button.textContent = action.label || 'Action';
                button.addEventListener('click', () => {
                    if (typeof action.onClick === 'function') {
                        action.onClick();
                    }
                    dismissToast(toast);
                });
                footer.appendChild(button);
            });
            toast.appendChild(footer);
        }

        if (dismissible) {
            const closeBtn = document.createElement('button');
            closeBtn.type = 'button';
            closeBtn.className = 'toast__close';
            closeBtn.setAttribute('aria-label', 'Dismiss notification');
            closeBtn.innerHTML = '&times;';
            closeBtn.addEventListener('click', () => dismissToast(toast));
            toast.appendChild(closeBtn);
        }

        host.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('toast--visible'));

        if (duration > 0) {
            setTimeout(() => dismissToast(toast), duration);
        }

        return {
            close: () => dismissToast(toast)
        };
    };

    return { show };
})();

function notify(message, options = {}) {
    return notificationCenter.show({ message, ...options });
}

function notifySuccess(message, options = {}) {
    return notify(message, { ...options, type: 'success' });
}

function notifyError(message, options = {}) {
    return notify(message, { ...options, type: 'error' });
}

function notifyWarning(message, options = {}) {
    return notify(message, { ...options, type: 'warning' });
}

function notifyInfo(message, options = {}) {
    return notify(message, { ...options, type: 'info' });
}

function confirmDialog({
    title = 'Please confirm',
    message = 'Are you sure?',
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    tone = 'warning'
} = {}) {
    return new Promise(resolve => {
        const overlay = document.createElement('div');
        overlay.className = 'confirm-overlay';

        const dialog = document.createElement('div');
        dialog.className = `confirm-dialog confirm-dialog--${tone}`;

        if (title) {
            const heading = document.createElement('h3');
            heading.textContent = title;
            dialog.appendChild(heading);
        }

        if (message) {
            const body = document.createElement('p');
            body.textContent = message;
            dialog.appendChild(body);
        }

        const actions = document.createElement('div');
        actions.className = 'confirm-dialog__actions';

        const cancelBtn = document.createElement('button');
        cancelBtn.type = 'button';
        cancelBtn.className = 'btn btn--ghost';
        cancelBtn.textContent = cancelText;

        const confirmBtn = document.createElement('button');
        confirmBtn.type = 'button';
        confirmBtn.className = 'btn btn--primary';
        confirmBtn.textContent = confirmText;

        const cleanup = (result) => {
            overlay.classList.remove('confirm-overlay--visible');
            setTimeout(() => overlay.remove(), 200);
            resolve(result);
        };

        cancelBtn.addEventListener('click', () => cleanup(false));
        confirmBtn.addEventListener('click', () => cleanup(true));
        overlay.addEventListener('click', (evt) => {
            if (evt.target === overlay) {
                cleanup(false);
            }
        });

        actions.appendChild(cancelBtn);
        actions.appendChild(confirmBtn);
        dialog.appendChild(actions);
        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        requestAnimationFrame(() => overlay.classList.add('confirm-overlay--visible'));
        confirmBtn.focus({ preventScroll: true });
    });
}

// ============================================================================
// USER SESSION MANAGEMENT
// ============================================================================

function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            return JSON.parse(userStr);
        } catch (e) {
            log.error('Failed to parse user data', e);
            return null;
        }
    }
    return null;
}

function isLoggedIn() {
    return getCurrentUser() !== null;
}

function isCustomer() {
    const user = getCurrentUser();
    return user && user.role === 'CUSTOMER';
}

function isVendor() {
    const user = getCurrentUser();
    return user && user.role === 'VENDOR';
}

// ============================================================================
// AUTHENTICATION API
// ============================================================================

async function apiLogin(username, password) {
    log.info('Login attempt', { username });
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        log.info(`Login response: ${response.status}`);

        if (response.ok) {
            const data = await response.json();
            log.success('Login successful', data);

            // Backend returns { message: "...", user: {...} }
            const userObj = data.user || data;

            // Store user data
            const userData = {
                id: userObj.id,
                username: userObj.username || username,
                role: userObj.role || 'CUSTOMER',
                pincode: userObj.pincode || '',
                email: userObj.email || '',
                phone: userObj.phone || '',
                address: userObj.address || '',
                loginTime: new Date().toISOString()
            };

            localStorage.setItem('user', JSON.stringify(userData));
            log.success('User data stored', userData);

            return { success: true, data: userData };
        } else {
            const errorText = await response.text();
            log.error('Login failed', errorText);
            return { success: false, error: errorText || 'Invalid credentials' };
        }
    } catch (error) {
        log.error('Login exception', error);
        return { success: false, error: error.message };
    }
}

async function apiRegister(userData) {
    log.info('Registration attempt', { username: userData.username });
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        log.info(`Registration response: ${response.status}`);

        if (response.ok) {
            const data = await response.json();
            log.success('Registration successful', data);
            return { success: true, data };
        } else {
            const errorText = await response.text();
            log.error('Registration failed', errorText);
            return { success: false, error: errorText || 'Registration failed' };
        }
    } catch (error) {
        log.error('Registration exception', error);
        return { success: false, error: error.message };
    }
}

function apiLogout() {
    log.info('Logging out user');
    localStorage.removeItem('user');
    sessionStorage.clear();
    log.success('User logged out');
}

// ============================================================================
// PRODUCTS API
// Fetch a single product by ID
async function apiGetProductById(productId) {
    log.info('Fetching product by ID', { productId });
    try {
        const url = `${API_BASE_URL}/products/${productId}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        log.info(`Product by ID response: ${response.status}`);
        if (response.ok) {
            const product = await response.json();
            log.success('Fetched product by ID', product);
            return { success: true, data: product };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch product by ID', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Product by ID fetch exception', error);
        return { success: false, error: error.message };
    }
}
// ============================================================================

async function apiGetProducts(pincode = null) {
    log.info('Fetching products', { pincode: pincode || 'all' });
    
    try {
        // Build URL - if pincode provided, add as query param
        let url = `${API_BASE_URL}/products`;
        if (pincode) {
            url += `?pincode=${encodeURIComponent(pincode)}`;
        }
            
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Products response: ${response.status}`);

        if (response.ok) {
            const products = await response.json();
            log.success(`Fetched ${products.length} products`);
            return { success: true, data: products };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch products', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Products fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiAddProduct(productData) {
    log.info('Adding product', productData);
    
    const user = getCurrentUser();
    if (!user || !user.pincode) {
        log.error('User pincode not available');
        notifyError('We couldn’t find your shop pincode. Please sign in again.');
        return { success: false, error: 'User pincode not available' };
    }

    if (!user.id) {
        log.error('User ID not available - old session detected');
        notifyError('Your session is from an old version. Clearing and redirecting to login...');
        // Auto-clear old session and redirect
        setTimeout(() => {
            localStorage.clear();
            window.location.href = 'login.html';
        }, 2000);
        return { success: false, error: 'User ID not available. Clearing session and redirecting...' };
    }

    // Add pincode and vendor info to product data
    const productWithPincode = {
        ...productData,
        pincode: user.pincode,
        vendor: {
            id: user.id
        }
    };
    
    log.info('Product data sent to backend:', productWithPincode);

    try {
        const response = await fetch(`${API_BASE_URL}/products`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(productWithPincode)
        });

        log.info(`Add product response: ${response.status}`);

        if (response.ok) {
            const product = await response.json();
            log.success('Product added successfully', product);
            return { success: true, data: product };
        } else {
            const errorText = await response.text();
            log.error('Failed to add product', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Add product exception', error);
        return { success: false, error: error.message };
    }
}

async function apiUpdateProduct(productId, productData) {
    log.info('Updating product', { productId, ...productData });
    
    try {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(productData)
        });

        log.info(`Update product response: ${response.status}`);

        if (response.ok) {
            const product = await response.json();
            log.success('Product updated successfully', product);
            return { success: true, data: product };
        } else {
            const errorText = await response.text();
            log.error('Failed to update product', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Update product exception', error);
        return { success: false, error: error.message };
    }
}

async function apiDeleteProduct(productId) {
    log.info('Deleting product', { productId });
    
    try {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Delete product response: ${response.status}`);

        if (response.ok) {
            log.success('Product deleted successfully');
            return { success: true };
        } else {
            const errorText = await response.text();
            log.error('Failed to delete product', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Delete product exception', error);
        return { success: false, error: error.message };
    }
}

// ============================================================================
// CART API
// ============================================================================

async function apiGetCart(username) {
    log.info('Fetching cart', { username });
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${username}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Cart response: ${response.status}`);

        if (response.ok) {
            const cart = await response.json();
            log.success(`Fetched ${cart.length} cart items`);
            return { success: true, data: cart };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch cart', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Cart fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiAddToCart(username, productId, quantity = 1) {
    log.info('Adding to cart', { username, productId, quantity });
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, productId, quantity })
        });

        log.info(`Add to cart response: ${response.status}`);

        if (response.ok) {
            const cartItem = await response.json();
            log.success('Added to cart successfully', cartItem);
            return { success: true, data: cartItem };
        } else {
            const errorText = await response.text();
            log.error('Failed to add to cart', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Add to cart exception', error);
        return { success: false, error: error.message };
    }
}

async function apiRemoveFromCart(cartItemId) {
    log.info('Removing from cart', { cartItemId });
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${cartItemId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Remove from cart response: ${response.status}`);

        if (response.ok) {
            log.success('Removed from cart successfully');
            return { success: true };
        } else {
            const errorText = await response.text();
            log.error('Failed to remove from cart', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Remove from cart exception', error);
        return { success: false, error: error.message };
    }
}

async function apiUpdateCartQuantity(cartItemId, quantity) {
    log.info('Updating cart quantity', { cartItemId, quantity });
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${cartItemId}/quantity`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ quantity })
        });

        log.info(`Update cart quantity response: ${response.status}`);

        if (response.ok) {
            const cartItem = await response.json();
            log.success('Cart quantity updated successfully', cartItem);
            return { success: true, data: cartItem };
        } else {
            const errorText = await response.text();
            log.error('Failed to update cart quantity', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Update cart quantity exception', error);
        return { success: false, error: error.message };
    }
}

// ============================================================================
// ORDERS API
// ============================================================================

async function apiPlaceOrder(username, selectedPincode = null) {
    log.info('Placing order', { username, selectedPincode });
    
    try {
        // Fetch addresses from API to get latest data
        let address = null;
        const addressResult = await apiGetAddresses(username);
        
        if (addressResult && addressResult.success && addressResult.data && addressResult.data.length > 0) {
            const addresses = addressResult.data;
            log.info('Fetched addresses for order', { count: addresses.length, selectedPincode });
            
            // CRITICAL: ONLY use address with selected pincode - NO fallback to default
            if (selectedPincode) {
                address = addresses.find(a => a.pincode === selectedPincode);
                if (address) {
                    log.success('Found address matching selected pincode', { pincode: selectedPincode });
                } else {
                    log.error('No address found for selected pincode', { pincode: selectedPincode });
                }
            } else {
                log.error('No pincode provided for order');
            }
        }
        
        const addressPayload = address ? {
            fullAddress: address.fullAddress,
            pincode: address.pincode,
            phone: address.phone
        } : null;
        
        log.info('Address payload for order', addressPayload);
        const response = await fetch(`${API_BASE_URL}/orders/place`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, address: addressPayload })
        });

        log.info(`Place order response: ${response.status}`);

        if (response.ok) {
            const order = await response.json();
            log.success('Order placed successfully', order);
            return { success: true, data: order };
        } else {
            const errorText = await response.text();
            log.error('Failed to place order', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Place order exception', error);
        return { success: false, error: error.message };
    }
}

async function apiGetOrders(username) {
    log.info('Fetching orders', { username });
    
    try {
        const response = await fetch(`${API_BASE_URL}/orders/${username}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Orders response: ${response.status}`);

        if (response.ok) {
            const orders = await response.json();
            log.success(`Fetched ${orders.length} orders`);
            return { success: true, data: orders };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch orders', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Orders fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiGetOrderDetails(orderId) {
    log.info('Fetching order details', { orderId });
    
    try {
        const response = await fetch(`${API_BASE_URL}/orders/details/${orderId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Order details response: ${response.status}`);

        if (response.ok) {
            const orderDetails = await response.json();
            log.success('Fetched order details');
            return { success: true, data: orderDetails };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch order details', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Order details fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiGetVendorOrders(username) {
    log.info('Fetching vendor orders', { username });
    
    try {
        const response = await fetch(`${API_BASE_URL}/orders/vendor/${username}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        log.info(`Vendor orders response: ${response.status}`);

        if (response.ok) {
            const orderItems = await response.json();
            log.success(`Fetched ${orderItems.length} vendor order items`);
            return { success: true, data: orderItems };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch vendor orders', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Vendor orders fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiUpdateOrderStatus(orderId, status) {
    log.info('Updating order status', { orderId, status });
    
    try {
        const response = await fetch(`${API_BASE_URL}/orders/status/${orderId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ status })
        });

        log.info(`Update order status response: ${response.status}`);

        if (response.ok) {
            const order = await response.json();
            log.success('Order status updated');
            return { success: true, data: order };
        } else {
            const errorText = await response.text();
            log.error('Failed to update order status', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Update order status exception', error);
        return { success: false, error: error.message };
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// USER API
async function apiGetUser(username) {
    log.info('Fetching user', { username });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        log.info(`User response: ${response.status}`);
        if (response.ok) {
            const user = await response.json();
            log.success('Fetched user', user);
            return { success: true, data: user };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch user', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('User fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiUpdateUser(username, userData) {
    log.info('Updating user', { username, userData });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        log.info(`Update user response: ${response.status}`);
        if (response.ok) {
            const user = await response.json();
            log.success('User updated', user);
            return { success: true, data: user };
        } else {
            const errorText = await response.text();
            log.error('Failed to update user', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('User update exception', error);
        return { success: false, error: error.message };
    }
}
// ============================================================================

function showLoading(show = true) {
    // You can implement a loading spinner here
    log.info(show ? 'Loading...' : 'Loading complete');
}

// ============================================================================
// ADDRESS API
// ============================================================================

async function apiGetAddresses(username) {
    log.info('Fetching addresses', { username });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}/addresses`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        log.info(`Addresses response: ${response.status}`);
        if (response.ok) {
            const addresses = await response.json();
            log.success(`Fetched ${addresses.length} addresses`);
            return { success: true, data: addresses };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch addresses', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Addresses fetch exception', error);
        return { success: false, error: error.message };
    }
}

async function apiAddAddress(username, addressData) {
    log.info('Adding address', { username, addressData });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}/addresses`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(addressData)
        });
        log.info(`Add address response: ${response.status}`);
        if (response.ok) {
            const address = await response.json();
            log.success('Address added successfully', address);
            return { success: true, data: address };
        } else {
            const errorText = await response.text();
            log.error('Failed to add address', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Add address exception', error);
        return { success: false, error: error.message };
    }
}

async function apiUpdateAddress(username, addressId, addressData) {
    log.info('Updating address', { username, addressId, addressData });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}/addresses/${addressId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(addressData)
        });
        log.info(`Update address response: ${response.status}`);
        if (response.ok) {
            const address = await response.json();
            log.success('Address updated successfully', address);
            return { success: true, data: address };
        } else {
            const errorText = await response.text();
            log.error('Failed to update address', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Update address exception', error);
        return { success: false, error: error.message };
    }
}

async function apiDeleteAddress(username, addressId) {
    log.info('Deleting address', { username, addressId });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}/addresses/${addressId}`, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });
        log.info(`Delete address response: ${response.status}`);
        if (response.ok) {
            log.success('Address deleted successfully');
            return { success: true };
        } else {
            const errorText = await response.text();
            log.error('Failed to delete address', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Delete address exception', error);
        return { success: false, error: error.message };
    }
}

async function apiSetDefaultAddress(username, addressId) {
    log.info('Setting default address', { username, addressId });
    try {
        const response = await fetch(`${API_BASE_URL}/users/${username}/addresses/${addressId}/default`, {
            method: 'PUT',
            headers: { 'Accept': 'application/json' }
        });
        log.info(`Set default address response: ${response.status}`);
        if (response.ok) {
            log.success('Default address set');
            return { success: true };
        } else {
            const errorText = await response.text();
            log.error('Failed to set default address', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Set default address exception', error);
        return { success: false, error: error.message };
    }
}

// Fetch products for a specific vendor
async function apiGetProductsByVendor(vendorId) {
    log.info('Fetching products for vendor', { vendorId });
    try {
        const url = `${API_BASE_URL}/products/vendor/${vendorId}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        log.info(`Vendor products response: ${response.status}`);
        if (response.ok) {
            const products = await response.json();
            log.success(`Fetched ${products.length} vendor products`);
            return { success: true, data: products };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch vendor products', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Vendor products fetch exception', error);
        return { success: false, error: error };
    }
}

// Fetch vendor notifications
async function apiGetVendorNotifications(vendorId) {
    log.info('Fetching vendor notifications', { vendorId });
    try {
        const url = `${API_BASE_URL}/notifications/vendor/${vendorId}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        log.info(`Vendor notifications response: ${response.status}`);
        if (response.ok) {
            const notifications = await response.json();
            log.success(`Fetched ${notifications.length} vendor notifications`);
            return { success: true, data: notifications };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch vendor notifications', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Vendor notifications fetch exception', error);
        return { success: false, error: error };
    }
}

// Fetch vendor out-of-stock indicator
async function apiGetVendorOutOfStockIndicator(vendorId) {
    log.info('Fetching vendor out-of-stock indicator', { vendorId });
    try {
        const url = `${API_BASE_URL}/products/vendor/${vendorId}/outofstock`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        log.info(`Vendor out-of-stock response: ${response.status}`);
        if (response.ok) {
            const outOfStock = await response.json();
            log.success(`Fetched ${outOfStock.length} out-of-stock products`);
            return { success: true, data: outOfStock };
        } else {
            const errorText = await response.text();
            log.error('Failed to fetch out-of-stock indicator', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Vendor out-of-stock fetch exception', error);
        return { success: false, error: error };
    }
}

// Mark a vendor notification as read
async function apiMarkNotificationRead(notificationId) {
    log.info('Marking notification as read', { notificationId });
    try {
        const url = `${API_BASE_URL}/notifications/${notificationId}/read`;
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        log.info(`Mark notification read response: ${response.status}`);
        if (response.ok) {
            log.success('Notification marked as read');
            return { success: true };
        } else {
            const errorText = await response.text();
            log.error('Failed to mark notification as read', errorText);
            return { success: false, error: errorText };
        }
    } catch (error) {
        log.error('Mark notification read exception', error);
        return { success: false, error: error };
    }
}

// Make functions globally available
window.QuickKartAPI = {
    // Session
    getCurrentUser,
    isLoggedIn,
    isCustomer,
    isVendor,
    // Auth
    apiLogin,
    apiRegister,
    apiLogout,
    // Products
    apiGetProducts,
    apiAddProduct,
    apiUpdateProduct,
    apiDeleteProduct,
    apiGetProductsByVendor,
    apiGetProductById,
    apiGetVendorNotifications,
    apiGetVendorOutOfStockIndicator,
    // Cart
    apiGetCart,
    apiAddToCart,
    apiRemoveFromCart,
    apiUpdateCartQuantity,
    // Orders
    apiPlaceOrder,
    apiGetOrders,
    apiGetOrderDetails,
    apiGetVendorOrders,
    apiUpdateOrderStatus,
    // Address
    apiGetAddresses,
    apiAddAddress,
    apiUpdateAddress,
    apiDeleteAddress,
    apiSetDefaultAddress,
    apiGetUser,
    apiUpdateUser,
    // Utils
    log,
    showLoading,
    notify,
    notifySuccess,
    notifyError,
    notifyWarning,
    notifyInfo,
    confirm: confirmDialog,
    apiMarkNotificationRead
};

log.success('QuickKart API ready for use');
