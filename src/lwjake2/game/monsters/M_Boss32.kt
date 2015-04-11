/*
 * Copyright (C) 1997-2001 Id Software, Inc.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package lwjake2.game.monsters

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.EntDieAdapter
import lwjake2.game.EntInteractAdapter
import lwjake2.game.EntPainAdapter
import lwjake2.game.EntThinkAdapter
import lwjake2.game.GameAI
import lwjake2.game.GameBase
import lwjake2.game.GameMisc
import lwjake2.game.GameUtil
import lwjake2.game.Monster
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.game.trace_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Boss32 {
    companion object {

        public val FRAME_attak101: Int = 0

        public val FRAME_attak102: Int = 1

        public val FRAME_attak103: Int = 2

        public val FRAME_attak104: Int = 3

        public val FRAME_attak105: Int = 4

        public val FRAME_attak106: Int = 5

        public val FRAME_attak107: Int = 6

        public val FRAME_attak108: Int = 7

        public val FRAME_attak109: Int = 8

        public val FRAME_attak110: Int = 9

        public val FRAME_attak111: Int = 10

        public val FRAME_attak112: Int = 11

        public val FRAME_attak113: Int = 12

        public val FRAME_attak114: Int = 13

        public val FRAME_attak115: Int = 14

        public val FRAME_attak116: Int = 15

        public val FRAME_attak117: Int = 16

        public val FRAME_attak118: Int = 17

        public val FRAME_attak201: Int = 18

        public val FRAME_attak202: Int = 19

        public val FRAME_attak203: Int = 20

        public val FRAME_attak204: Int = 21

        public val FRAME_attak205: Int = 22

        public val FRAME_attak206: Int = 23

        public val FRAME_attak207: Int = 24

        public val FRAME_attak208: Int = 25

        public val FRAME_attak209: Int = 26

        public val FRAME_attak210: Int = 27

        public val FRAME_attak211: Int = 28

        public val FRAME_attak212: Int = 29

        public val FRAME_attak213: Int = 30

        public val FRAME_death01: Int = 31

        public val FRAME_death02: Int = 32

        public val FRAME_death03: Int = 33

        public val FRAME_death04: Int = 34

        public val FRAME_death05: Int = 35

        public val FRAME_death06: Int = 36

        public val FRAME_death07: Int = 37

        public val FRAME_death08: Int = 38

        public val FRAME_death09: Int = 39

        public val FRAME_death10: Int = 40

        public val FRAME_death11: Int = 41

        public val FRAME_death12: Int = 42

        public val FRAME_death13: Int = 43

        public val FRAME_death14: Int = 44

        public val FRAME_death15: Int = 45

        public val FRAME_death16: Int = 46

        public val FRAME_death17: Int = 47

        public val FRAME_death18: Int = 48

        public val FRAME_death19: Int = 49

        public val FRAME_death20: Int = 50

        public val FRAME_death21: Int = 51

        public val FRAME_death22: Int = 52

        public val FRAME_death23: Int = 53

        public val FRAME_death24: Int = 54

        public val FRAME_death25: Int = 55

        public val FRAME_death26: Int = 56

        public val FRAME_death27: Int = 57

        public val FRAME_death28: Int = 58

        public val FRAME_death29: Int = 59

        public val FRAME_death30: Int = 60

        public val FRAME_death31: Int = 61

        public val FRAME_death32: Int = 62

        public val FRAME_death33: Int = 63

        public val FRAME_death34: Int = 64

        public val FRAME_death35: Int = 65

        public val FRAME_death36: Int = 66

        public val FRAME_death37: Int = 67

        public val FRAME_death38: Int = 68

        public val FRAME_death39: Int = 69

        public val FRAME_death40: Int = 70

        public val FRAME_death41: Int = 71

        public val FRAME_death42: Int = 72

        public val FRAME_death43: Int = 73

        public val FRAME_death44: Int = 74

        public val FRAME_death45: Int = 75

        public val FRAME_death46: Int = 76

        public val FRAME_death47: Int = 77

        public val FRAME_death48: Int = 78

        public val FRAME_death49: Int = 79

        public val FRAME_death50: Int = 80

        public val FRAME_pain101: Int = 81

        public val FRAME_pain102: Int = 82

        public val FRAME_pain103: Int = 83

        public val FRAME_pain201: Int = 84

        public val FRAME_pain202: Int = 85

        public val FRAME_pain203: Int = 86

        public val FRAME_pain301: Int = 87

        public val FRAME_pain302: Int = 88

        public val FRAME_pain303: Int = 89

        public val FRAME_pain304: Int = 90

        public val FRAME_pain305: Int = 91

        public val FRAME_pain306: Int = 92

        public val FRAME_pain307: Int = 93

        public val FRAME_pain308: Int = 94

        public val FRAME_pain309: Int = 95

        public val FRAME_pain310: Int = 96

        public val FRAME_pain311: Int = 97

        public val FRAME_pain312: Int = 98

        public val FRAME_pain313: Int = 99

        public val FRAME_pain314: Int = 100

        public val FRAME_pain315: Int = 101

        public val FRAME_pain316: Int = 102

        public val FRAME_pain317: Int = 103

        public val FRAME_pain318: Int = 104

        public val FRAME_pain319: Int = 105

        public val FRAME_pain320: Int = 106

        public val FRAME_pain321: Int = 107

        public val FRAME_pain322: Int = 108

        public val FRAME_pain323: Int = 109

        public val FRAME_pain324: Int = 110

        public val FRAME_pain325: Int = 111

        public val FRAME_stand01: Int = 112

        public val FRAME_stand02: Int = 113

        public val FRAME_stand03: Int = 114

        public val FRAME_stand04: Int = 115

        public val FRAME_stand05: Int = 116

        public val FRAME_stand06: Int = 117

        public val FRAME_stand07: Int = 118

        public val FRAME_stand08: Int = 119

        public val FRAME_stand09: Int = 120

        public val FRAME_stand10: Int = 121

        public val FRAME_stand11: Int = 122

        public val FRAME_stand12: Int = 123

        public val FRAME_stand13: Int = 124

        public val FRAME_stand14: Int = 125

        public val FRAME_stand15: Int = 126

        public val FRAME_stand16: Int = 127

        public val FRAME_stand17: Int = 128

        public val FRAME_stand18: Int = 129

        public val FRAME_stand19: Int = 130

        public val FRAME_stand20: Int = 131

        public val FRAME_stand21: Int = 132

        public val FRAME_stand22: Int = 133

        public val FRAME_stand23: Int = 134

        public val FRAME_stand24: Int = 135

        public val FRAME_stand25: Int = 136

        public val FRAME_stand26: Int = 137

        public val FRAME_stand27: Int = 138

        public val FRAME_stand28: Int = 139

        public val FRAME_stand29: Int = 140

        public val FRAME_stand30: Int = 141

        public val FRAME_stand31: Int = 142

        public val FRAME_stand32: Int = 143

        public val FRAME_stand33: Int = 144

        public val FRAME_stand34: Int = 145

        public val FRAME_stand35: Int = 146

        public val FRAME_stand36: Int = 147

        public val FRAME_stand37: Int = 148

        public val FRAME_stand38: Int = 149

        public val FRAME_stand39: Int = 150

        public val FRAME_stand40: Int = 151

        public val FRAME_stand41: Int = 152

        public val FRAME_stand42: Int = 153

        public val FRAME_stand43: Int = 154

        public val FRAME_stand44: Int = 155

        public val FRAME_stand45: Int = 156

        public val FRAME_stand46: Int = 157

        public val FRAME_stand47: Int = 158

        public val FRAME_stand48: Int = 159

        public val FRAME_stand49: Int = 160

        public val FRAME_stand50: Int = 161

        public val FRAME_stand51: Int = 162

        public val FRAME_walk01: Int = 163

        public val FRAME_walk02: Int = 164

        public val FRAME_walk03: Int = 165

        public val FRAME_walk04: Int = 166

        public val FRAME_walk05: Int = 167

        public val FRAME_walk06: Int = 168

        public val FRAME_walk07: Int = 169

        public val FRAME_walk08: Int = 170

        public val FRAME_walk09: Int = 171

        public val FRAME_walk10: Int = 172

        public val FRAME_walk11: Int = 173

        public val FRAME_walk12: Int = 174

        public val FRAME_walk13: Int = 175

        public val FRAME_walk14: Int = 176

        public val FRAME_walk15: Int = 177

        public val FRAME_walk16: Int = 178

        public val FRAME_walk17: Int = 179

        public val FRAME_walk18: Int = 180

        public val FRAME_walk19: Int = 181

        public val FRAME_walk20: Int = 182

        public val FRAME_walk21: Int = 183

        public val FRAME_walk22: Int = 184

        public val FRAME_walk23: Int = 185

        public val FRAME_walk24: Int = 186

        public val FRAME_walk25: Int = 187

        public val FRAME_active01: Int = 188

        public val FRAME_active02: Int = 189

        public val FRAME_active03: Int = 190

        public val FRAME_active04: Int = 191

        public val FRAME_active05: Int = 192

        public val FRAME_active06: Int = 193

        public val FRAME_active07: Int = 194

        public val FRAME_active08: Int = 195

        public val FRAME_active09: Int = 196

        public val FRAME_active10: Int = 197

        public val FRAME_active11: Int = 198

        public val FRAME_active12: Int = 199

        public val FRAME_active13: Int = 200

        public val FRAME_attak301: Int = 201

        public val FRAME_attak302: Int = 202

        public val FRAME_attak303: Int = 203

        public val FRAME_attak304: Int = 204

        public val FRAME_attak305: Int = 205

        public val FRAME_attak306: Int = 206

        public val FRAME_attak307: Int = 207

        public val FRAME_attak308: Int = 208

        public val FRAME_attak401: Int = 209

        public val FRAME_attak402: Int = 210

        public val FRAME_attak403: Int = 211

        public val FRAME_attak404: Int = 212

        public val FRAME_attak405: Int = 213

        public val FRAME_attak406: Int = 214

        public val FRAME_attak407: Int = 215

        public val FRAME_attak408: Int = 216

        public val FRAME_attak409: Int = 217

        public val FRAME_attak410: Int = 218

        public val FRAME_attak411: Int = 219

        public val FRAME_attak412: Int = 220

        public val FRAME_attak413: Int = 221

        public val FRAME_attak414: Int = 222

        public val FRAME_attak415: Int = 223

        public val FRAME_attak416: Int = 224

        public val FRAME_attak417: Int = 225

        public val FRAME_attak418: Int = 226

        public val FRAME_attak419: Int = 227

        public val FRAME_attak420: Int = 228

        public val FRAME_attak421: Int = 229

        public val FRAME_attak422: Int = 230

        public val FRAME_attak423: Int = 231

        public val FRAME_attak424: Int = 232

        public val FRAME_attak425: Int = 233

        public val FRAME_attak426: Int = 234

        public val FRAME_attak501: Int = 235

        public val FRAME_attak502: Int = 236

        public val FRAME_attak503: Int = 237

        public val FRAME_attak504: Int = 238

        public val FRAME_attak505: Int = 239

        public val FRAME_attak506: Int = 240

        public val FRAME_attak507: Int = 241

        public val FRAME_attak508: Int = 242

        public val FRAME_attak509: Int = 243

        public val FRAME_attak510: Int = 244

        public val FRAME_attak511: Int = 245

        public val FRAME_attak512: Int = 246

        public val FRAME_attak513: Int = 247

        public val FRAME_attak514: Int = 248

        public val FRAME_attak515: Int = 249

        public val FRAME_attak516: Int = 250

        public val FRAME_death201: Int = 251

        public val FRAME_death202: Int = 252

        public val FRAME_death203: Int = 253

        public val FRAME_death204: Int = 254

        public val FRAME_death205: Int = 255

        public val FRAME_death206: Int = 256

        public val FRAME_death207: Int = 257

        public val FRAME_death208: Int = 258

        public val FRAME_death209: Int = 259

        public val FRAME_death210: Int = 260

        public val FRAME_death211: Int = 261

        public val FRAME_death212: Int = 262

        public val FRAME_death213: Int = 263

        public val FRAME_death214: Int = 264

        public val FRAME_death215: Int = 265

        public val FRAME_death216: Int = 266

        public val FRAME_death217: Int = 267

        public val FRAME_death218: Int = 268

        public val FRAME_death219: Int = 269

        public val FRAME_death220: Int = 270

        public val FRAME_death221: Int = 271

        public val FRAME_death222: Int = 272

        public val FRAME_death223: Int = 273

        public val FRAME_death224: Int = 274

        public val FRAME_death225: Int = 275

        public val FRAME_death226: Int = 276

        public val FRAME_death227: Int = 277

        public val FRAME_death228: Int = 278

        public val FRAME_death229: Int = 279

        public val FRAME_death230: Int = 280

        public val FRAME_death231: Int = 281

        public val FRAME_death232: Int = 282

        public val FRAME_death233: Int = 283

        public val FRAME_death234: Int = 284

        public val FRAME_death235: Int = 285

        public val FRAME_death236: Int = 286

        public val FRAME_death237: Int = 287

        public val FRAME_death238: Int = 288

        public val FRAME_death239: Int = 289

        public val FRAME_death240: Int = 290

        public val FRAME_death241: Int = 291

        public val FRAME_death242: Int = 292

        public val FRAME_death243: Int = 293

        public val FRAME_death244: Int = 294

        public val FRAME_death245: Int = 295

        public val FRAME_death246: Int = 296

        public val FRAME_death247: Int = 297

        public val FRAME_death248: Int = 298

        public val FRAME_death249: Int = 299

        public val FRAME_death250: Int = 300

        public val FRAME_death251: Int = 301

        public val FRAME_death252: Int = 302

        public val FRAME_death253: Int = 303

        public val FRAME_death254: Int = 304

        public val FRAME_death255: Int = 305

        public val FRAME_death256: Int = 306

        public val FRAME_death257: Int = 307

        public val FRAME_death258: Int = 308

        public val FRAME_death259: Int = 309

        public val FRAME_death260: Int = 310

        public val FRAME_death261: Int = 311

        public val FRAME_death262: Int = 312

        public val FRAME_death263: Int = 313

        public val FRAME_death264: Int = 314

        public val FRAME_death265: Int = 315

        public val FRAME_death266: Int = 316

        public val FRAME_death267: Int = 317

        public val FRAME_death268: Int = 318

        public val FRAME_death269: Int = 319

        public val FRAME_death270: Int = 320

        public val FRAME_death271: Int = 321

        public val FRAME_death272: Int = 322

        public val FRAME_death273: Int = 323

        public val FRAME_death274: Int = 324

        public val FRAME_death275: Int = 325

        public val FRAME_death276: Int = 326

        public val FRAME_death277: Int = 327

        public val FRAME_death278: Int = 328

        public val FRAME_death279: Int = 329

        public val FRAME_death280: Int = 330

        public val FRAME_death281: Int = 331

        public val FRAME_death282: Int = 332

        public val FRAME_death283: Int = 333

        public val FRAME_death284: Int = 334

        public val FRAME_death285: Int = 335

        public val FRAME_death286: Int = 336

        public val FRAME_death287: Int = 337

        public val FRAME_death288: Int = 338

        public val FRAME_death289: Int = 339

        public val FRAME_death290: Int = 340

        public val FRAME_death291: Int = 341

        public val FRAME_death292: Int = 342

        public val FRAME_death293: Int = 343

        public val FRAME_death294: Int = 344

        public val FRAME_death295: Int = 345

        public val FRAME_death301: Int = 346

        public val FRAME_death302: Int = 347

        public val FRAME_death303: Int = 348

        public val FRAME_death304: Int = 349

        public val FRAME_death305: Int = 350

        public val FRAME_death306: Int = 351

        public val FRAME_death307: Int = 352

        public val FRAME_death308: Int = 353

        public val FRAME_death309: Int = 354

        public val FRAME_death310: Int = 355

        public val FRAME_death311: Int = 356

        public val FRAME_death312: Int = 357

        public val FRAME_death313: Int = 358

        public val FRAME_death314: Int = 359

        public val FRAME_death315: Int = 360

        public val FRAME_death316: Int = 361

        public val FRAME_death317: Int = 362

        public val FRAME_death318: Int = 363

        public val FRAME_death319: Int = 364

        public val FRAME_death320: Int = 365

        public val FRAME_jump01: Int = 366

        public val FRAME_jump02: Int = 367

        public val FRAME_jump03: Int = 368

        public val FRAME_jump04: Int = 369

        public val FRAME_jump05: Int = 370

        public val FRAME_jump06: Int = 371

        public val FRAME_jump07: Int = 372

        public val FRAME_jump08: Int = 373

        public val FRAME_jump09: Int = 374

        public val FRAME_jump10: Int = 375

        public val FRAME_jump11: Int = 376

        public val FRAME_jump12: Int = 377

        public val FRAME_jump13: Int = 378

        public val FRAME_pain401: Int = 379

        public val FRAME_pain402: Int = 380

        public val FRAME_pain403: Int = 381

        public val FRAME_pain404: Int = 382

        public val FRAME_pain501: Int = 383

        public val FRAME_pain502: Int = 384

        public val FRAME_pain503: Int = 385

        public val FRAME_pain504: Int = 386

        public val FRAME_pain601: Int = 387

        public val FRAME_pain602: Int = 388

        public val FRAME_pain603: Int = 389

        public val FRAME_pain604: Int = 390

        public val FRAME_pain605: Int = 391

        public val FRAME_pain606: Int = 392

        public val FRAME_pain607: Int = 393

        public val FRAME_pain608: Int = 394

        public val FRAME_pain609: Int = 395

        public val FRAME_pain610: Int = 396

        public val FRAME_pain611: Int = 397

        public val FRAME_pain612: Int = 398

        public val FRAME_pain613: Int = 399

        public val FRAME_pain614: Int = 400

        public val FRAME_pain615: Int = 401

        public val FRAME_pain616: Int = 402

        public val FRAME_pain617: Int = 403

        public val FRAME_pain618: Int = 404

        public val FRAME_pain619: Int = 405

        public val FRAME_pain620: Int = 406

        public val FRAME_pain621: Int = 407

        public val FRAME_pain622: Int = 408

        public val FRAME_pain623: Int = 409

        public val FRAME_pain624: Int = 410

        public val FRAME_pain625: Int = 411

        public val FRAME_pain626: Int = 412

        public val FRAME_pain627: Int = 413

        public val FRAME_stand201: Int = 414

        public val FRAME_stand202: Int = 415

        public val FRAME_stand203: Int = 416

        public val FRAME_stand204: Int = 417

        public val FRAME_stand205: Int = 418

        public val FRAME_stand206: Int = 419

        public val FRAME_stand207: Int = 420

        public val FRAME_stand208: Int = 421

        public val FRAME_stand209: Int = 422

        public val FRAME_stand210: Int = 423

        public val FRAME_stand211: Int = 424

        public val FRAME_stand212: Int = 425

        public val FRAME_stand213: Int = 426

        public val FRAME_stand214: Int = 427

        public val FRAME_stand215: Int = 428

        public val FRAME_stand216: Int = 429

        public val FRAME_stand217: Int = 430

        public val FRAME_stand218: Int = 431

        public val FRAME_stand219: Int = 432

        public val FRAME_stand220: Int = 433

        public val FRAME_stand221: Int = 434

        public val FRAME_stand222: Int = 435

        public val FRAME_stand223: Int = 436

        public val FRAME_stand224: Int = 437

        public val FRAME_stand225: Int = 438

        public val FRAME_stand226: Int = 439

        public val FRAME_stand227: Int = 440

        public val FRAME_stand228: Int = 441

        public val FRAME_stand229: Int = 442

        public val FRAME_stand230: Int = 443

        public val FRAME_stand231: Int = 444

        public val FRAME_stand232: Int = 445

        public val FRAME_stand233: Int = 446

        public val FRAME_stand234: Int = 447

        public val FRAME_stand235: Int = 448

        public val FRAME_stand236: Int = 449

        public val FRAME_stand237: Int = 450

        public val FRAME_stand238: Int = 451

        public val FRAME_stand239: Int = 452

        public val FRAME_stand240: Int = 453

        public val FRAME_stand241: Int = 454

        public val FRAME_stand242: Int = 455

        public val FRAME_stand243: Int = 456

        public val FRAME_stand244: Int = 457

        public val FRAME_stand245: Int = 458

        public val FRAME_stand246: Int = 459

        public val FRAME_stand247: Int = 460

        public val FRAME_stand248: Int = 461

        public val FRAME_stand249: Int = 462

        public val FRAME_stand250: Int = 463

        public val FRAME_stand251: Int = 464

        public val FRAME_stand252: Int = 465

        public val FRAME_stand253: Int = 466

        public val FRAME_stand254: Int = 467

        public val FRAME_stand255: Int = 468

        public val FRAME_stand256: Int = 469

        public val FRAME_stand257: Int = 470

        public val FRAME_stand258: Int = 471

        public val FRAME_stand259: Int = 472

        public val FRAME_stand260: Int = 473

        public val FRAME_walk201: Int = 474

        public val FRAME_walk202: Int = 475

        public val FRAME_walk203: Int = 476

        public val FRAME_walk204: Int = 477

        public val FRAME_walk205: Int = 478

        public val FRAME_walk206: Int = 479

        public val FRAME_walk207: Int = 480

        public val FRAME_walk208: Int = 481

        public val FRAME_walk209: Int = 482

        public val FRAME_walk210: Int = 483

        public val FRAME_walk211: Int = 484

        public val FRAME_walk212: Int = 485

        public val FRAME_walk213: Int = 486

        public val FRAME_walk214: Int = 487

        public val FRAME_walk215: Int = 488

        public val FRAME_walk216: Int = 489

        public val FRAME_walk217: Int = 490

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain4: Int = 0

        var sound_pain5: Int = 0

        var sound_pain6: Int = 0

        var sound_death: Int = 0

        var sound_step_left: Int = 0

        var sound_step_right: Int = 0

        var sound_attack_bfg: Int = 0

        var sound_brainsplorch: Int = 0

        var sound_prerailgun: Int = 0

        var sound_popup: Int = 0

        var sound_taunt1: Int = 0

        var sound_taunt2: Int = 0

        var sound_taunt3: Int = 0

        var sound_hit: Int = 0

        var makron_taunt: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_taunt"
            }

            public fun think(self: edict_t): Boolean {
                val r: Float

                r = Lib.random()
                if (r <= 0.3)
                    GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_taunt1, 1, Defines.ATTN_NONE, 0)
                else if (r <= 0.6)
                    GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_taunt2, 1, Defines.ATTN_NONE, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_taunt3, 1, Defines.ATTN_NONE, 0)
                return true
            }
        }

        //
        //	   stand
        //
        var makron_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = makron_move_stand
                return true
            }
        }

        /*
     * static EntThinkAdapter xxx = new EntThinkAdapter() { public boolean
     * think(edict_t self) { return true; } };
     */

        var makron_hit: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_hit"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_hit, 1, Defines.ATTN_NONE, 0)
                return true
            }
        }

        var makron_popup: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_popup"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_popup, 1, Defines.ATTN_NONE, 0)
                return true
            }
        }

        var makron_step_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_step_left"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_step_left, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var makron_step_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_step_right"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_step_right, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var makron_brainsplorch: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_brainsplorch"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_brainsplorch, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var makron_prerailgun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_prerailgun"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_prerailgun, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var makron_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 10
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 20
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 30
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 40
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 50
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null) // 60
        )

        var makron_move_stand = mmove_t(FRAME_stand201, FRAME_stand260, makron_frames_stand, null)

        var makron_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 3, makron_step_left), mframe_t(GameAI.ai_run, 12, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, makron_step_right), mframe_t(GameAI.ai_run, 6, null), mframe_t(GameAI.ai_run, 12, null), mframe_t(GameAI.ai_run, 9, null), mframe_t(GameAI.ai_run, 6, null), mframe_t(GameAI.ai_run, 12, null))

        var makron_move_run = mmove_t(FRAME_walk204, FRAME_walk213, makron_frames_run, null)

        var makron_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 3, makron_step_left), mframe_t(GameAI.ai_walk, 12, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, makron_step_right), mframe_t(GameAI.ai_walk, 6, null), mframe_t(GameAI.ai_walk, 12, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, 6, null), mframe_t(GameAI.ai_walk, 12, null))

        var makron_move_walk = mmove_t(FRAME_walk204, FRAME_walk213, makron_frames_run, null)

        //
        //	   death
        //
        var makron_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_dead"
            }

            public fun think(self: edict_t): Boolean {
                Math3D.VectorSet(self.mins, -60, -60, 0)
                Math3D.VectorSet(self.maxs, 60, 60, 72)
                self.movetype = Defines.MOVETYPE_TOSS
                self.svflags = self.svflags or Defines.SVF_DEADMONSTER
                self.nextthink = 0
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var makron_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = makron_move_walk
                return true
            }
        }

        var makron_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = makron_move_stand
                else
                    self.monsterinfo.currentmove = makron_move_run
                return true
            }
        }

        var makron_frames_pain6 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 10
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, makron_popup), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 20
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, makron_taunt), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_pain6 = mmove_t(FRAME_pain601, FRAME_pain627, makron_frames_pain6, makron_run)

        var makron_frames_pain5 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_pain5 = mmove_t(FRAME_pain501, FRAME_pain504, makron_frames_pain5, makron_run)

        var makron_frames_pain4 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_pain4 = mmove_t(FRAME_pain401, FRAME_pain404, makron_frames_pain4, makron_run)

        var makron_frames_death2 = array<mframe_t>(mframe_t(GameAI.ai_move, -15, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, -12, null), mframe_t(GameAI.ai_move, 0, makron_step_left), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 10
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 11, null), mframe_t(GameAI.ai_move, 12, null), mframe_t(GameAI.ai_move, 11, makron_step_right), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 20
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 30
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 5, null), mframe_t(GameAI.ai_move, 7, null), mframe_t(GameAI.ai_move, 6, makron_step_left), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, 2, null), // 40
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 50
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -6, makron_step_right), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -4, makron_step_left), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 60
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, -5, null), mframe_t(GameAI.ai_move, -3, makron_step_right), mframe_t(GameAI.ai_move, -8, null), mframe_t(GameAI.ai_move, -3, makron_step_left), mframe_t(GameAI.ai_move, -7, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -4, makron_step_right), // 70
                mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -7, null), mframe_t(GameAI.ai_move, 0, makron_step_left), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 80
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null), // 90
                mframe_t(GameAI.ai_move, 27, makron_hit), mframe_t(GameAI.ai_move, 26, null), mframe_t(GameAI.ai_move, 0, makron_brainsplorch), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null) // 95
        )

        var makron_move_death2 = mmove_t(FRAME_death201, FRAME_death295, makron_frames_death2, makron_dead)

        var makron_frames_death3 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_death3 = mmove_t(FRAME_death301, FRAME_death320, makron_frames_death3, null)

        var makron_frames_sight = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_sight = mmove_t(FRAME_active01, FRAME_active13, makron_frames_sight, makron_run)

        var makronBFG: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makronBFG"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)

                val start = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val vec = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_MAKRON_BFG], forward, right, start)

                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_attack_bfg, 1, Defines.ATTN_NORM, 0)
                Monster.monster_fire_bfg(self, start, dir, 50, 300, 100, 300, Defines.MZ2_MAKRON_BFG)
                return true
            }
        }

        var MakronSaveloc: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "MakronSaveloc"
            }

            public fun think(self: edict_t): Boolean {
                Math3D.VectorCopy(self.enemy.s.origin, self.pos1) //save for
                // aiming the
                // shot
                self.pos1[2] += self.enemy.viewheight
                return true
            }
        }

        //	   FIXME: He's not firing from the proper Z

        var MakronRailgun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "MakronRailgun"
            }

            public fun think(self: edict_t): Boolean {
                val start = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_MAKRON_RAILGUN_1], forward, right, start)

                // calc direction to where we targted
                Math3D.VectorSubtract(self.pos1, start, dir)
                Math3D.VectorNormalize(dir)

                Monster.monster_fire_railgun(self, start, dir, 50, 100, Defines.MZ2_MAKRON_RAILGUN_1)

                return true
            }
        }

        //	   FIXME: This is all wrong. He's not firing at the proper angles.

        var MakronHyperblaster: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "MakronHyperblaster"
            }

            public fun think(self: edict_t): Boolean {
                val dir = floatArray(0.0, 0.0, 0.0)
                val vec = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val flash_number: Int

                flash_number = Defines.MZ2_MAKRON_BLASTER_1 + (self.s.frame - FRAME_attak405)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[flash_number], forward, right, start)

                if (self.enemy != null) {
                    Math3D.VectorCopy(self.enemy.s.origin, vec)
                    vec[2] += self.enemy.viewheight
                    Math3D.VectorSubtract(vec, start, vec)
                    Math3D.vectoangles(vec, vec)
                    dir[0] = vec[0]
                } else {
                    dir[0] = 0
                }
                if (self.s.frame <= FRAME_attak413)
                    dir[1] = self.s.angles[1] - 10 * (self.s.frame - FRAME_attak413)
                else
                    dir[1] = self.s.angles[1] + 10 * (self.s.frame - FRAME_attak421)
                dir[2] = 0

                Math3D.AngleVectors(dir, forward, null, null)

                Monster.monster_fire_blaster(self, start, forward, 15, 1000, Defines.MZ2_MAKRON_BLASTER_1, Defines.EF_BLASTER)

                return true
            }
        }

        var makron_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "makron_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {

                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                // Lessen the chance of him going into his pain frames
                if (damage <= 25)
                    if (Lib.random() < 0.2)
                        return

                self.pain_debounce_time = GameBase.level.time + 3
                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (damage <= 40) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain4, 1, Defines.ATTN_NONE, 0)
                    self.monsterinfo.currentmove = makron_move_pain4
                } else if (damage <= 110) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain5, 1, Defines.ATTN_NONE, 0)
                    self.monsterinfo.currentmove = makron_move_pain5
                } else {
                    if (damage <= 150)
                        if (Lib.random() <= 0.45) {
                            GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain6, 1, Defines.ATTN_NONE, 0)
                            self.monsterinfo.currentmove = makron_move_pain6
                        } else if (Lib.random() <= 0.35) {
                            GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain6, 1, Defines.ATTN_NONE, 0)
                            self.monsterinfo.currentmove = makron_move_pain6
                        }
                }
            }

        }

        var makron_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "makron_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                self.monsterinfo.currentmove = makron_move_sight
                return true
            }
        }

        var makron_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_attack"
            }

            public fun think(self: edict_t): Boolean {
                val vec = floatArray(0.0, 0.0, 0.0)
                val r: Float

                r = Lib.random()

                Math3D.VectorSubtract(self.enemy.s.origin, self.s.origin, vec)

                if (r <= 0.3)
                    self.monsterinfo.currentmove = makron_move_attack3
                else if (r <= 0.6)
                    self.monsterinfo.currentmove = makron_move_attack4
                else
                    self.monsterinfo.currentmove = makron_move_attack5

                return true
            }
        }

        /*
     * --- Makron Torso. This needs to be spawned in ---
     */

        var makron_torso_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_torso_think"
            }

            public fun think(self: edict_t): Boolean {
                if (++self.s.frame < 365)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else {
                    self.s.frame = 346
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                }
                return true
            }
        }

        var makron_torso: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "makron_torso"
            }

            public fun think(ent: edict_t): Boolean {
                ent.movetype = Defines.MOVETYPE_NONE
                ent.solid = Defines.SOLID_NOT
                Math3D.VectorSet(ent.mins, -8, -8, 0)
                Math3D.VectorSet(ent.maxs, 8, 8, 8)
                ent.s.frame = 346
                ent.s.modelindex = GameBase.gi.modelindex("models/monsters/boss3/rider/tris.md2")
                ent.think = makron_torso_think
                ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
                ent.s.sound = GameBase.gi.soundindex("makron/spine.wav")
                GameBase.gi.linkentity(ent)
                return true
            }
        }

        var makron_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "makron_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                val tempent: edict_t

                var n: Int

                self.s.sound = 0
                // check for gib
                if (self.health <= self.gib_health) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, GameBase.gi.soundindex("misc/udeath.wav"), 1, Defines.ATTN_NORM, 0)
                    run {
                        n = 0
                        while (n < 1 /* 4 */) {
                            GameMisc.ThrowGib(self, "models/objects/gibs/sm_meat/tris.md2", damage, Defines.GIB_ORGANIC)
                            n++
                        }
                    }
                    run {
                        n = 0
                        while (n < 4) {
                            GameMisc.ThrowGib(self, "models/objects/gibs/sm_metal/tris.md2", damage, Defines.GIB_METALLIC)
                            n++
                        }
                    }
                    GameMisc.ThrowHead(self, "models/objects/gibs/gear/tris.md2", damage, Defines.GIB_METALLIC)
                    self.deadflag = Defines.DEAD_DEAD
                    return
                }

                if (self.deadflag == Defines.DEAD_DEAD)
                    return

                //	   regular death
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death, 1, Defines.ATTN_NONE, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES

                tempent = GameUtil.G_Spawn()
                Math3D.VectorCopy(self.s.origin, tempent.s.origin)
                Math3D.VectorCopy(self.s.angles, tempent.s.angles)
                tempent.s.origin[1] -= 84
                makron_torso.think(tempent)

                self.monsterinfo.currentmove = makron_move_death2
            }
        }

        var Makron_CheckAttack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Makron_CheckAttack"
            }

            public fun think(self: edict_t): Boolean {
                val spot1 = floatArray(0.0, 0.0, 0.0)
                val spot2 = floatArray(0.0, 0.0, 0.0)
                val temp = floatArray(0.0, 0.0, 0.0)
                val chance: Float
                val tr: trace_t
                val enemy_range: Int
                val enemy_yaw: Float

                if (self.enemy.health > 0) {
                    // see if any entities are in the way of the shot
                    Math3D.VectorCopy(self.s.origin, spot1)
                    spot1[2] += self.viewheight
                    Math3D.VectorCopy(self.enemy.s.origin, spot2)
                    spot2[2] += self.enemy.viewheight

                    tr = GameBase.gi.trace(spot1, null, null, spot2, self, Defines.CONTENTS_SOLID or Defines.CONTENTS_MONSTER or Defines.CONTENTS_SLIME or Defines.CONTENTS_LAVA)

                    // do we have a clear shot?
                    if (tr.ent != self.enemy)
                        return false
                }

                enemy_range = GameUtil.range(self, self.enemy)
                Math3D.VectorSubtract(self.enemy.s.origin, self.s.origin, temp)
                enemy_yaw = Math3D.vectoyaw(temp)

                self.ideal_yaw = enemy_yaw

                // melee attack
                if (enemy_range == Defines.RANGE_MELEE) {
                    if (self.monsterinfo.melee != null)
                        self.monsterinfo.attack_state = Defines.AS_MELEE
                    else
                        self.monsterinfo.attack_state = Defines.AS_MISSILE
                    return true
                }

                //	   missile attack
                if (null != self.monsterinfo.attack)
                    return false

                if (GameBase.level.time < self.monsterinfo.attack_finished)
                    return false

                if (enemy_range == Defines.RANGE_FAR)
                    return false

                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0) {
                    chance = 0.4.toFloat()
                } else if (enemy_range == Defines.RANGE_MELEE) {
                    chance = 0.8.toFloat()
                } else if (enemy_range == Defines.RANGE_NEAR) {
                    chance = 0.4.toFloat()
                } else if (enemy_range == Defines.RANGE_MID) {
                    chance = 0.2.toFloat()
                } else {
                    return false
                }

                if (Lib.random() < chance) {
                    self.monsterinfo.attack_state = Defines.AS_MISSILE
                    self.monsterinfo.attack_finished = GameBase.level.time + 2 * Lib.random()
                    return true
                }

                if ((self.flags and Defines.FL_FLY) != 0) {
                    if (Lib.random() < 0.3)
                        self.monsterinfo.attack_state = Defines.AS_SLIDING
                    else
                        self.monsterinfo.attack_state = Defines.AS_STRAIGHT
                }

                return false
            }
        }

        var makron_frames_attack3 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, makronBFG), // FIXME: BFG Attack here
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_attack3 = mmove_t(FRAME_attak301, FRAME_attak308, makron_frames_attack3, makron_run)

        var makron_frames_attack4 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, MakronHyperblaster), // fire
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_attack4 = mmove_t(FRAME_attak401, FRAME_attak426, makron_frames_attack4, makron_run)

        var makron_frames_attack5 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, makron_prerailgun), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, MakronSaveloc), mframe_t(GameAI.ai_move, 0, MakronRailgun), // Fire railgun
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var makron_move_attack5 = mmove_t(FRAME_attak501, FRAME_attak516, makron_frames_attack5, makron_run)

        /*
     * ================= MakronSpawn
     * 
     * =================
     */
        var MakronSpawn: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "MakronSpawn"
            }

            public fun think(self: edict_t): Boolean {
                val vec = floatArray(0.0, 0.0, 0.0)

                val player: edict_t?

                SP_monster_makron(self)

                // jump at player
                player = GameBase.level.sight_client
                if (player == null)
                    return true

                Math3D.VectorSubtract(player!!.s.origin, self.s.origin, vec)
                self.s.angles[Defines.YAW] = Math3D.vectoyaw(vec)
                Math3D.VectorNormalize(vec)
                Math3D.VectorMA(Globals.vec3_origin, 400, vec, self.velocity)
                self.velocity[2] = 200
                self.groundentity = null

                return true
            }
        }

        var MakronToss: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "MakronToss"
            }

            public fun think(self: edict_t): Boolean {
                val ent: edict_t

                ent = GameUtil.G_Spawn()
                ent.nextthink = GameBase.level.time + 0.8.toFloat()
                ent.think = MakronSpawn
                ent.target = self.target
                Math3D.VectorCopy(self.s.origin, ent.s.origin)
                return true
            }
        }

        //
        //	   monster_makron
        //

        fun MakronPrecache() {
            sound_pain4 = GameBase.gi.soundindex("makron/pain3.wav")
            sound_pain5 = GameBase.gi.soundindex("makron/pain2.wav")
            sound_pain6 = GameBase.gi.soundindex("makron/pain1.wav")
            sound_death = GameBase.gi.soundindex("makron/death.wav")
            sound_step_left = GameBase.gi.soundindex("makron/step1.wav")
            sound_step_right = GameBase.gi.soundindex("makron/step2.wav")
            sound_attack_bfg = GameBase.gi.soundindex("makron/bfg_fire.wav")
            sound_brainsplorch = GameBase.gi.soundindex("makron/brain1.wav")
            sound_prerailgun = GameBase.gi.soundindex("makron/rail_up.wav")
            sound_popup = GameBase.gi.soundindex("makron/popup.wav")
            sound_taunt1 = GameBase.gi.soundindex("makron/voice4.wav")
            sound_taunt2 = GameBase.gi.soundindex("makron/voice3.wav")
            sound_taunt3 = GameBase.gi.soundindex("makron/voice.wav")
            sound_hit = GameBase.gi.soundindex("makron/bhit.wav")

            GameBase.gi.modelindex("models/monsters/boss3/rider/tris.md2")
        }

        /*
     * QUAKED monster_makron (1 .5 0) (-30 -30 0) (30 30 90) Ambush
     * Trigger_Spawn Sight
     */
        fun SP_monster_makron(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            MakronPrecache()

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/boss3/rider/tris.md2")
            Math3D.VectorSet(self.mins, -30, -30, 0)
            Math3D.VectorSet(self.maxs, 30, 30, 90)

            self.health = 3000
            self.gib_health = -2000
            self.mass = 500

            self.pain = makron_pain
            self.die = makron_die
            self.monsterinfo.stand = makron_stand
            self.monsterinfo.walk = makron_walk
            self.monsterinfo.run = makron_run
            self.monsterinfo.dodge = null
            self.monsterinfo.attack = makron_attack
            self.monsterinfo.melee = null
            self.monsterinfo.sight = makron_sight
            self.monsterinfo.checkattack = Makron_CheckAttack

            GameBase.gi.linkentity(self)

            //		self.monsterinfo.currentmove = &makron_move_stand;
            self.monsterinfo.currentmove = makron_move_sight
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.walkmonster_start.think(self)
        }
    }
}