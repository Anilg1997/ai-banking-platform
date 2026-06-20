const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

const BASE_URL = 'http://localhost:4200';
const SCREENSHOTS_DIR = path.join(__dirname, 'screenshots');

// Mock user data for authenticated pages
const MOCK_USER = {
  id: 'user-001',
  username: 'john.doe',
  email: 'john@example.com',
  firstName: 'John',
  lastName: 'Doe',
  roles: ['ROLE_USER'],
  emailVerified: true,
  twoFactorEnabled: false,
};

const MOCK_ADMIN = {
  ...MOCK_USER,
  roles: ['ROLE_ADMIN'],
};

const MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTAwMSIsInVzZXJuYW1lIjoiam9obi5kb2UiLCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.mock';

if (!fs.existsSync(SCREENSHOTS_DIR)) {
  fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
}

async function waitAndScreenshot(page, url, name, isAuth = false, isAdmin = false) {
  console.log(`\n📸 Taking screenshot: ${name} (${url})`);
  try {
    await page.goto(`${BASE_URL}${url}`, { waitUntil: 'networkidle2', timeout: 30000 });
    
    // If auth page, inject mock auth data
    if (isAuth || isAdmin) {
      const user = isAdmin ? MOCK_ADMIN : MOCK_USER;
      const tokenKey = 'novabank_token';
      const refreshTokenKey = 'novabank_refresh_token';
      const userKey = 'novabank_user';
      
      await page.evaluate(({ tokenKey, refreshTokenKey, userKey, user, token }) => {
        localStorage.setItem(tokenKey, token);
        localStorage.setItem(refreshTokenKey, token);
        localStorage.setItem(userKey, JSON.stringify(user));
      }, { tokenKey, refreshTokenKey, userKey, user: isAdmin ? MOCK_ADMIN : MOCK_USER, token: MOCK_TOKEN });
      
      // Reload to pick up auth state
      await page.goto(`${BASE_URL}${url}`, { waitUntil: 'networkidle2', timeout: 30000 });
    }
    
    // Wait for content to render
    await new Promise(r => setTimeout(r, 3000));
    
    // Take full page screenshot
    const filePath = path.join(SCREENSHOTS_DIR, `${name}.png`);
    await page.screenshot({ 
      path: filePath,
      fullPage: true,
      type: 'png'
    });
    console.log(`  ✅ Saved: ${filePath}`);
    return true;
  } catch (err) {
    console.error(`  ❌ Error: ${err.message}`);
    return false;
  }
}

(async () => {
  console.log('🚀 Starting NovaBank Screenshot Capture...\n');
  
  const browser = await puppeteer.launch({
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1920,1080']
  });
  
  const page = await browser.newPage();
  await page.setViewport({ width: 1920, height: 1080 });

  const results = [];

  // ===== PUBLIC PAGES =====
  results.push({ name: 'Screenshot 1', result: await waitAndScreenshot(page, '/login', 'login') });
  results.push({ name: 'Screenshot 2', result: await waitAndScreenshot(page, '/register', 'register') });

  // ===== USER PAGES (authenticated) =====
  results.push({ name: 'Screenshot 3', result: await waitAndScreenshot(page, '/dashboard', 'dashboard', true) });
  results.push({ name: 'Screenshot 4', result: await waitAndScreenshot(page, '/accounts', 'accounts', true) });
  results.push({ name: 'Screenshot 5', result: await waitAndScreenshot(page, '/transactions', 'transactions', true) });
  results.push({ name: 'Screenshot 6', result: await waitAndScreenshot(page, '/cards', 'cards', true) });
  results.push({ name: 'Screenshot 7', result: await waitAndScreenshot(page, '/cards/apply', 'card-apply', true) });
  results.push({ name: 'Screenshot 8', result: await waitAndScreenshot(page, '/loans', 'loans', true) });
  results.push({ name: 'Screenshot 9', result: await waitAndScreenshot(page, '/profile', 'profile', true) });
  results.push({ name: 'Screenshot 10', result: await waitAndScreenshot(page, '/notifications', 'notifications', true) });

  // ===== ADMIN PAGES (admin auth) =====
  results.push({ name: 'Screenshot 11', result: await waitAndScreenshot(page, '/admin/dashboard', 'admin-dashboard', true, true) });
  results.push({ name: 'Screenshot 12', result: await waitAndScreenshot(page, '/admin/users', 'admin-users', true, true) });
  results.push({ name: 'Screenshot 13', result: await waitAndScreenshot(page, '/admin/cards', 'admin-cards', true, true) });
  results.push({ name: 'Screenshot 14', result: await waitAndScreenshot(page, '/admin/agent', 'admin-agent', true, true) });
  results.push({ name: 'Screenshot 15', result: await waitAndScreenshot(page, '/admin/settings', 'admin-settings', true, true) });

  // ===== AI CHAT =====
  console.log('\n📸 Taking screenshot: ai-chat');
  try {
    await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'networkidle2', timeout: 30000 });
    // Inject mock auth and reload
    await page.evaluate(({ user, token }) => {
      localStorage.setItem('novabank_token', token);
      localStorage.setItem('novabank_refresh_token', token);
      localStorage.setItem('novabank_user', JSON.stringify(user));
    }, { user: MOCK_USER, token: MOCK_TOKEN });
    await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'networkidle2', timeout: 30000 });
    await new Promise(r => setTimeout(r, 3000));
    
    // Click the AI chat FAB to open it
    const fabBtn = await page.$('.chat-fab');
    if (fabBtn) {
      await fabBtn.click();
      await new Promise(r => setTimeout(r, 2000));
    }
    
    await page.screenshot({ 
      path: path.join(SCREENSHOTS_DIR, 'ai-chat.png'),
      fullPage: false,
      type: 'png'
    });
    console.log('  ✅ Saved: ai-chat.png');
    results.push({ name: 'Screenshot 16', result: true });
  } catch (err) {
    console.error(`  ❌ Error: ${err.message}`);
    results.push({ name: 'Screenshot 16', result: false });
  }

  await browser.close();

  // Summary
  console.log('\n========================================');
  console.log('📊 SCREENSHOT CAPTURE SUMMARY');
  console.log('========================================');
  const success = results.filter(r => r.result).length;
  const failed = results.filter(r => !r.result).length;
  results.forEach(r => console.log(`  ${r.result ? '✅' : '❌'} ${r.name}`));
  console.log(`\nTotal: ${results.length} | Success: ${success} | Failed: ${failed}`);
  console.log(`Screenshots saved to: ${SCREENSHOTS_DIR}`);
})();
