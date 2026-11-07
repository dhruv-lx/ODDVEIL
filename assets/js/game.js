/**
 * ===================================================================
 * SQUID MARBLES: LEO'S GAMBIT (Portfolio Edition)
 * Core Game Engine, Procedural Audio, and Interactive Logic
 * ===================================================================
 */

// --- AUDIO SYNTHESIZER (No external MP3s needed) ---
class AudioEngine {
  constructor() {
    this.ctx = null;
    this.enabled = true;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) this.ctx = new AudioCtx();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playMarbleClink() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    const t = this.ctx.currentTime;
    // Glass clink: high resonant bandpass ping
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = 'sine';
    const pitch = 2200 + Math.random() * 800;
    osc.frequency.setValueAtTime(pitch, t);
    osc.frequency.exponentialRampToValueAtTime(1400, t + 0.08);

    gain.gain.setValueAtTime(0.25, t);
    gain.gain.exponentialRampToValueAtTime(0.001, t + 0.1);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start(t);
    osc.stop(t + 0.1);
  }

  playWinFanfare() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
    notes.forEach((freq, idx) => {
      const t = this.ctx.currentTime + (idx * 0.09);
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, t);

      gain.gain.setValueAtTime(0.2, t);
      gain.gain.exponentialRampToValueAtTime(0.001, t + 0.18);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(t);
      osc.stop(t + 0.2);
    });
  }

  playDefeatTone() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    const notes = [440, 392, 349.23, 293.66];
    notes.forEach((freq, idx) => {
      const t = this.ctx.currentTime + (idx * 0.11);
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(freq, t);

      gain.gain.setValueAtTime(0.15, t);
      gain.gain.exponentialRampToValueAtTime(0.001, t + 0.22);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(t);
      osc.stop(t + 0.25);
    });
  }
}

// --- GAME CONTROLLER ---
class MarbleGame {
  constructor() {
    this.audio = new AudioEngine();

    // Game variables
    this.playerName = localStorage.getItem('squid_marble_name') || 'Player';
    this.playerMarbles = 10;
    this.leoMarbles = 10;
    this.round = 1;
    this.currentStreak = 0;
    this.highestStreak = 0;
    this.biggestBetWon = 0;
    this.difficulty = 'strategic';

    // State
    this.isPlayerAttacking = true; // Odd rounds = Player bets & guesses; Even rounds = Player hides
    this.leoHidden = 0;

    // DOM Elements
    this.initElements();
    this.bindEvents();
    this.renderAll();
    this.setupRound();
  }

  initElements() {
    // Header
    this.roundNumberEl = document.getElementById('roundNumber');
    this.streakBadgeEl = document.getElementById('streakBadge');
    this.streakCountEl = document.getElementById('streakCount');
    this.difficultySelect = document.getElementById('difficultySelect');
    this.soundToggleBtn = document.getElementById('soundToggleBtn');
    this.resetGameBtn = document.getElementById('resetGameBtn');

    // Fighter cards
    this.leoAvatar = document.getElementById('leoAvatar');
    this.leoStatusTag = document.getElementById('leoStatusTag');
    this.leoMarbleCount = document.getElementById('leoMarbleCount');
    this.leoPouch = document.getElementById('leoPouch');
    this.leoDialogue = document.getElementById('leoDialogue');

    this.playerNameDisplay = document.getElementById('playerNameDisplay');
    this.playerMarbleCount = document.getElementById('playerMarbleCount');
    this.playerPouch = document.getElementById('playerPouch');
    this.editNameBtn = document.getElementById('editNameBtn');
    this.tipText = document.getElementById('tipText');
    this.turnPhasePill = document.getElementById('turnPhasePill');

    // Controls
    this.actionTitle = document.getElementById('actionTitle');
    this.actionSubtitle = document.getElementById('actionSubtitle');
    this.attackControls = document.getElementById('attackControls');
    this.defenseControls = document.getElementById('defenseControls');

    this.betSlider = document.getElementById('betSlider');
    this.betDisplay = document.getElementById('betDisplay');
    this.maxBetMarker = document.getElementById('maxBetMarker');
    this.guessOddBtn = document.getElementById('guessOddBtn');
    this.guessEvenBtn = document.getElementById('guessEvenBtn');

    this.hideSlider = document.getElementById('hideSlider');
    this.hideDisplay = document.getElementById('hideDisplay');
    this.maxHideMarker = document.getElementById('maxHideMarker');
    this.submitHideBtn = document.getElementById('submitHideBtn');

    // Log & Accolades
    this.logStream = document.getElementById('logStream');
    this.clearLogBtn = document.getElementById('clearLogBtn');
    this.badgeFirstWin = document.getElementById('badgeFirstWin');
    this.badgeStreak3 = document.getElementById('badgeStreak3');
    this.badgeHighRoller = document.getElementById('badgeHighRoller');
    this.badgeChampion = document.getElementById('badgeChampion');

    // Modals
    this.revealModal = document.getElementById('revealModal');
    this.modalIcon = document.getElementById('modalIcon');
    this.modalTitle = document.getElementById('modalTitle');
    this.modalDescription = document.getElementById('modalDescription');
    this.revealedMarblesRow = document.getElementById('revealedMarblesRow');
    this.modalPoints = document.getElementById('modalPoints');
    this.modalContinueBtn = document.getElementById('modalContinueBtn');

    this.gameOverModal = document.getElementById('gameOverModal');
    this.gameOverIcon = document.getElementById('gameOverIcon');
    this.gameOverTitle = document.getElementById('gameOverTitle');
    this.gameOverDesc = document.getElementById('gameOverDesc');
    this.summaryRounds = document.getElementById('summaryRounds');
    this.summaryStreak = document.getElementById('summaryStreak');
    this.summaryMaxBet = document.getElementById('summaryMaxBet');
    this.playAgainBtn = document.getElementById('playAgainBtn');
  }

  bindEvents() {
    this.playerNameDisplay.textContent = this.playerName;

    // Change Name
    this.editNameBtn.addEventListener('click', () => {
      const newName = prompt('Enter your player nickname:', this.playerName);
      if (newName && newName.trim()) {
        this.playerName = newName.trim();
        this.playerNameDisplay.textContent = this.playerName;
        localStorage.setItem('squid_marble_name', this.playerName);
        this.addLog(`Player renamed to "${this.playerName}"`, 'system');
      }
    });

    // Sound toggle
    this.soundToggleBtn.addEventListener('click', () => {
      this.audio.enabled = !this.audio.enabled;
      this.soundToggleBtn.textContent = this.audio.enabled ? '🔊' : '🔇';
      if (this.audio.enabled) this.audio.playMarbleClink();
    });

    // Difficulty switch
    this.difficultySelect.addEventListener('change', (e) => {
      this.difficulty = e.target.value;
      const modeNames = {
        casual: 'Casual Leo (Easy)',
        strategic: 'Strategic Leo (Normal)',
        mindreader: 'Mind Reader (Hard)'
      };
      this.setLeoDialogue(`AI mode switched to ${modeNames[this.difficulty]}! Prepare yourself.`);
      this.addLog(`AI Difficulty set to ${modeNames[this.difficulty]}`, 'system');
    });

    // Reset button
    this.resetGameBtn.addEventListener('click', () => {
      if (confirm('Are you sure you want to reset the current match?')) {
        this.resetGame();
      }
    });

    // Bet slider
    this.betSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      this.betDisplay.textContent = `${val} Marble${val > 1 ? 's' : ''}`;
      this.audio.playMarbleClink();
    });

    // Hide slider
    this.hideSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      this.hideDisplay.textContent = `${val} Marble${val > 1 ? 's' : ''}`;
      this.audio.playMarbleClink();
    });

    // Guess buttons
    this.guessOddBtn.addEventListener('click', () => this.handlePlayerGuess(1));
    this.guessEvenBtn.addEventListener('click', () => this.handlePlayerGuess(2));

    // Hide submit button (Player defense)
    this.submitHideBtn.addEventListener('click', () => this.handlePlayerHide());

    // Modal continue
    this.modalContinueBtn.addEventListener('click', () => {
      this.revealModal.classList.add('hidden');
      this.checkGameOverOrNext();
    });

    // Play again
    this.playAgainBtn.addEventListener('click', () => {
      this.gameOverModal.classList.add('hidden');
      this.resetGame();
    });

    // Clear log
    this.clearLogBtn.addEventListener('click', () => {
      this.logStream.innerHTML = '';
      this.addLog('Log cleared.', 'system');
    });
  }

  // --- ROUND MANAGEMENT ---
  setupRound() {
    this.roundNumberEl.textContent = this.round;
    this.streakCountEl.textContent = this.currentStreak;

    // Odd rounds: Player attacks (Leo hides, Player bets & guesses)
    // Even rounds: Player defends (Player hides, Leo bets & guesses)
    this.isPlayerAttacking = (this.round % 2 !== 0);

    const maxBet = Math.min(this.playerMarbles, this.leoMarbles);

    if (this.isPlayerAttacking) {
      // Player attack
      this.turnPhasePill.textContent = 'YOUR TURN TO ATTACK';
      this.turnPhasePill.style.color = 'var(--accent-cyan)';
      this.turnPhasePill.style.borderColor = 'rgba(45, 212, 191, 0.4)';
      this.turnPhasePill.style.background = 'rgba(45, 212, 191, 0.15)';

      this.actionTitle.textContent = `Round ${this.round}: Place Your Bet & Guess Leo's Fist`;
      this.actionSubtitle.textContent = `Leo is hiding marbles. How many will you wager, and is his count ODD or EVEN?`;

      this.attackControls.classList.remove('hidden');
      this.defenseControls.classList.add('hidden');

      this.betSlider.max = maxBet;
      this.maxBetMarker.textContent = maxBet;
      if (parseInt(this.betSlider.value, 10) > maxBet) {
        this.betSlider.value = maxBet;
      }
      this.betDisplay.textContent = `${this.betSlider.value} Marble${this.betSlider.value > 1 ? 's' : ''}`;

      // Leo secretly hides a number of marbles from 1 to his total
      this.leoHidden = Math.floor(Math.random() * this.leoMarbles) + 1;
      this.setLeoDialogueForAttack();
    } else {
      // Leo attack (Player defense)
      this.turnPhasePill.textContent = "LEO'S TURN TO ATTACK";
      this.turnPhasePill.style.color = 'var(--accent-coral)';
      this.turnPhasePill.style.borderColor = 'rgba(244, 63, 94, 0.4)';
      this.turnPhasePill.style.background = 'rgba(244, 63, 94, 0.15)';

      this.actionTitle.textContent = `Round ${this.round}: Close Your Fist & Let Leo Guess`;
      this.actionSubtitle.textContent = `Select how many marbles to conceal in your hand. Leo will wager and guess!`;

      this.attackControls.classList.add('hidden');
      this.defenseControls.classList.remove('hidden');

      this.hideSlider.max = this.playerMarbles;
      this.maxHideMarker.textContent = this.playerMarbles;
      if (parseInt(this.hideSlider.value, 10) > this.playerMarbles) {
        this.hideSlider.value = this.playerMarbles;
      }
      this.hideDisplay.textContent = `${this.hideSlider.value} Marble${this.hideSlider.value > 1 ? 's' : ''}`;

      this.setLeoDialogue(`Now the tables have turned, ${this.playerName}! Hide your marbles and let me see your poker face!`);
    }

    this.updateLeoPersonality();
    this.renderAll();
  }

  // --- PLAYER GUESS RESOLUTION (Player Attack) ---
  handlePlayerGuess(guessType) { // 1 = Odd, 2 = Even
    const bet = parseInt(this.betSlider.value, 10);
    const isOdd = (this.leoHidden % 2 !== 0);
    const actualTypeName = isOdd ? 'ODD' : 'EVEN';
    const guessTypeName = (guessType === 1) ? 'ODD' : 'EVEN';
    const correct = (guessType === 1 && isOdd) || (guessType === 2 && !isOdd);

    if (correct) {
      this.audio.playWinFanfare();
      this.playerMarbles += bet;
      this.leoMarbles -= bet;
      this.currentStreak++;
      if (this.currentStreak > this.highestStreak) this.highestStreak = this.currentStreak;
      if (bet > this.biggestBetWon) this.biggestBetWon = bet;

      this.checkAchievements(bet);

      this.addLog(`R${this.round}: You correctly guessed ${guessTypeName}! Leo had ${this.leoHidden} (${actualTypeName}). Won +${bet} marbles.`, 'win');
      this.showRevealModal(true, this.leoHidden, actualTypeName, bet, `You guessed ${guessTypeName} correctly! Leo revealed ${this.leoHidden} marble(s).`);
      this.setLeoDialogue(this.getRandomDialogue([
        `No way! How did you read that?! Take your ${bet} marbles...`,
        `Agh! You saw right through my bluff!`,
        `Lucky guess, ${this.playerName}! Next round won't be so easy.`,
        `Curses! That was supposed to be my trick number!`
      ]));
    } else {
      this.audio.playDefeatTone();
      this.playerMarbles -= bet;
      this.leoMarbles += bet;
      this.currentStreak = 0;

      this.addLog(`R${this.round}: You guessed ${guessTypeName} incorrectly. Leo had ${this.leoHidden} (${actualTypeName}). Lost -${bet} marbles.`, 'loss');
      this.showRevealModal(false, this.leoHidden, actualTypeName, bet, `Leo was hiding ${this.leoHidden} marble(s) (${actualTypeName}). You guessed ${guessTypeName}.`);
      this.setLeoDialogue(this.getRandomDialogue([
        `Haha! Caught you! Thanks for the ${bet} shiny marbles!`,
        `You fell right into my trap! +${bet} for Leo!`,
        `Gganbu! Your marbles look mighty fine in my bag!`,
        `Classic mistake! You overthought it.`
      ]));
    }
  }

  // --- LEO GUESS RESOLUTION (Player Defense) ---
  handlePlayerHide() {
    const playerHidden = parseInt(this.hideSlider.value, 10);
    const maxLeoBet = Math.min(this.leoMarbles, this.playerMarbles);

    // Calculate Leo's bet
    let leoBet = 1;
    if (this.difficulty === 'casual') {
      leoBet = Math.floor(Math.random() * maxLeoBet) + 1;
    } else if (this.difficulty === 'strategic') {
      leoBet = Math.max(1, Math.floor(Math.random() * Math.ceil(maxLeoBet / 2)) + 1);
    } else {
      // Mind reader: aggressive betting
      leoBet = Math.max(1, Math.min(maxLeoBet, Math.ceil(maxLeoBet * 0.7)));
    }

    // Calculate Leo's guess
    const playerIsOdd = (playerHidden % 2 !== 0);
    const playerTypeName = playerIsOdd ? 'ODD' : 'EVEN';

    let smartChance = 0.5;
    if (this.difficulty === 'casual') smartChance = 0.35;
    if (this.difficulty === 'strategic') smartChance = 0.65;
    if (this.difficulty === 'mindreader') smartChance = 0.82;

    const roll = Math.random();
    let leoGuess = 1; // 1 = Odd, 2 = Even
    if (roll < smartChance) {
      leoGuess = playerIsOdd ? 1 : 2; // Smart guess
    } else {
      leoGuess = (Math.random() > 0.5) ? 1 : 2; // Random guess
    }

    const leoGuessName = (leoGuess === 1) ? 'ODD' : 'EVEN';
    const leoCorrect = (leoGuess === 1 && playerIsOdd) || (leoGuess === 2 && !playerIsOdd);

    if (leoCorrect) {
      this.audio.playDefeatTone();
      this.leoMarbles += leoBet;
      this.playerMarbles -= leoBet;
      this.currentStreak = 0;

      this.addLog(`R${this.round}: Leo bet ${leoBet} and guessed ${leoGuessName} correctly! (You hid ${playerHidden}). Lost -${leoBet} marbles.`, 'loss');
      this.showRevealModal(false, playerHidden, playerTypeName, leoBet, `Leo bet ${leoBet} and guessed ${leoGuessName} correctly!`);
      this.setLeoDialogue(`Boom! I read you like an open book! +${leoBet} marbles for me.`);
    } else {
      this.audio.playWinFanfare();
      this.playerMarbles += leoBet;
      this.leoMarbles -= leoBet;
      this.currentStreak++;
      if (this.currentStreak > this.highestStreak) this.highestStreak = this.currentStreak;
      if (leoBet > this.biggestBetWon) this.biggestBetWon = leoBet;

      this.checkAchievements(leoBet);

      this.addLog(`R${this.round}: Leo bet ${leoBet} and guessed ${leoGuessName} wrong! (You hid ${playerHidden}). Won +${leoBet} marbles!`, 'win');
      this.showRevealModal(true, playerHidden, playerTypeName, leoBet, `Leo bet ${leoBet} and guessed ${leoGuessName} WRONGLY! You defended successfully.`);
      this.setLeoDialogue(`Darn it! You tricked me! How could I misread that?!`);
    }
  }

  // --- REVEAL MODAL DISPLAY ---
  showRevealModal(won, count, typeName, bet, description) {
    this.modalIcon.textContent = won ? '🎉' : '💀';
    this.modalTitle.textContent = won ? 'ROUND WON!' : 'ROUND LOST';
    this.modalTitle.style.color = won ? 'var(--accent-cyan)' : 'var(--accent-coral)';
    this.modalDescription.textContent = description;

    this.modalPoints.textContent = (won ? `+${bet}` : `-${bet}`) + ' Marbles';
    this.modalPoints.className = 'modal-points ' + (won ? '' : 'negative');

    // Render revealed marbles row
    this.revealedMarblesRow.innerHTML = '';
    for (let i = 0; i < count; i++) {
      const marble = document.createElement('div');
      marble.className = 'marble ' + (won ? 'cyan' : 'coral');
      this.revealedMarblesRow.appendChild(marble);
    }

    this.revealModal.classList.remove('hidden');
  }

  // --- GAME OVER OR ADVANCE ---
  checkGameOverOrNext() {
    this.renderAll();

    if (this.playerMarbles <= 0) {
      this.audio.playDefeatTone();
      this.showGameOverModal(false);
    } else if (this.leoMarbles <= 0) {
      this.audio.playWinFanfare();
      this.unlockBadge(this.badgeChampion);
      this.showGameOverModal(true);
    } else {
      this.round++;
      this.setupRound();
    }
  }

  showGameOverModal(victory) {
    this.gameOverIcon.textContent = victory ? '🏆' : '☠️';
    this.gameOverTitle.textContent = victory
      ? 'GGANBU MASTER! YOU CONQUERED LEO!'
      : 'ELIMINATED! LEO CLAIMED ALL MARBLES';
    this.gameOverTitle.style.color = victory ? 'var(--accent-gold)' : 'var(--accent-coral)';
    this.gameOverDesc.textContent = victory
      ? `Phenomenal intuition, ${this.playerName}! You stripped Leo of all 20 marbles and survived the arena!`
      : `You ran out of marbles, ${this.playerName}. Leo proved too sharp this time!`;

    this.summaryRounds.textContent = this.round;
    this.summaryStreak.textContent = this.highestStreak;
    this.summaryMaxBet.textContent = this.biggestBetWon;

    this.gameOverModal.classList.remove('hidden');
  }

  resetGame() {
    this.playerMarbles = 10;
    this.leoMarbles = 10;
    this.round = 1;
    this.currentStreak = 0;
    this.setupRound();
    this.addLog('Game reset. 10 marbles each.', 'system');
    this.setLeoDialogue(`Fresh start! Let's see who holds all 20 marbles at the end.`);
  }

  // --- ACCOLADES & LOGS ---
  checkAchievements(bet) {
    this.unlockBadge(this.badgeFirstWin);
    if (this.currentStreak >= 3) {
      this.unlockBadge(this.badgeStreak3);
    }
    if (bet >= 5) {
      this.unlockBadge(this.badgeHighRoller);
    }
  }

  unlockBadge(el) {
    if (el && el.classList.contains('locked')) {
      el.classList.remove('locked');
      el.classList.add('unlocked');
      this.addLog(`🏆 Accolade Unlocked: ${el.querySelector('.b-name').textContent}!`, 'win');
    }
  }

  addLog(msg, type = '') {
    const entry = document.createElement('div');
    entry.className = `log-entry ${type}`;
    entry.textContent = msg;
    this.logStream.appendChild(entry);
    this.logStream.scrollTop = this.logStream.scrollHeight;
  }

  // --- VISUAL RENDERING ---
  renderAll() {
    this.playerMarbleCount.textContent = this.playerMarbles;
    this.leoMarbleCount.textContent = this.leoMarbles;

    // Render Leo pouch marbles
    this.leoPouch.innerHTML = '';
    for (let i = 0; i < this.leoMarbles; i++) {
      const m = document.createElement('div');
      m.className = 'marble coral';
      this.leoPouch.appendChild(m);
    }

    // Render Player pouch marbles
    this.playerPouch.innerHTML = '';
    for (let i = 0; i < this.playerMarbles; i++) {
      const m = document.createElement('div');
      m.className = 'marble cyan';
      this.playerPouch.appendChild(m);
    }
  }

  updateLeoPersonality() {
    if (this.leoMarbles <= 3) {
      this.leoAvatar.textContent = '😰';
      this.leoStatusTag.textContent = 'Panicking';
      this.leoStatusTag.style.color = 'var(--accent-coral)';
      this.leoStatusTag.style.background = 'rgba(244, 63, 94, 0.2)';
    } else if (this.leoMarbles >= 15) {
      this.leoAvatar.textContent = '😎';
      this.leoStatusTag.textContent = 'Smug';
      this.leoStatusTag.style.color = 'var(--accent-cyan)';
      this.leoStatusTag.style.background = 'rgba(45, 212, 191, 0.2)';
    } else {
      this.leoAvatar.textContent = '🤖';
      this.leoStatusTag.textContent = 'Confident';
      this.leoStatusTag.style.color = 'var(--accent-gold)';
      this.leoStatusTag.style.background = 'rgba(251, 191, 36, 0.2)';
    }
  }

  setLeoDialogue(text) {
    this.leoDialogue.textContent = `"${text}"`;
  }

  setLeoDialogueForAttack() {
    const attackLines = [
      "I've got a tricky number in my closed fist. Odd or Even?",
      "Think carefully... I know what you picked last round!",
      "Are you feeling lucky, or will your bet be modest?",
      "Poker face on. You'll never crack this number!",
      "Odd or Even... make your choice!"
    ];
    this.setLeoDialogue(this.getRandomDialogue(attackLines));
  }

  getRandomDialogue(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
  }
}

// Start game when page loads
window.addEventListener('DOMContentLoaded', () => {
  window.game = new MarbleGame();
});

