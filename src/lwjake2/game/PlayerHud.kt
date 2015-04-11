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

package lwjake2.game

import lwjake2.Defines
import lwjake2.qcommon.Com
import lwjake2.util.Lib
import lwjake2.util.Math3D
import lwjake2.util.Vargs

public class PlayerHud {
    companion object {

        /*
     * ======================================================================
     * 
     * INTERMISSION
     * 
     * ======================================================================
     */

        public fun MoveClientToIntermission(ent: edict_t) {
            if (GameBase.deathmatch.value != 0 || GameBase.coop.value != 0)
                ent.client.showscores = true
            Math3D.VectorCopy(GameBase.level.intermission_origin, ent.s.origin)
            ent.client.ps.pmove.origin[0] = (GameBase.level.intermission_origin[0] * 8) as Short
            ent.client.ps.pmove.origin[1] = (GameBase.level.intermission_origin[1] * 8) as Short
            ent.client.ps.pmove.origin[2] = (GameBase.level.intermission_origin[2] * 8) as Short
            Math3D.VectorCopy(GameBase.level.intermission_angle, ent.client.ps.viewangles)
            ent.client.ps.pmove.pm_type = Defines.PM_FREEZE
            ent.client.ps.gunindex = 0
            ent.client.ps.blend[3] = 0
            ent.client.ps.rdflags = ent.client.ps.rdflags and Defines.RDF_UNDERWATER.inv()

            // clean up powerup info
            ent.client.quad_framenum = 0
            ent.client.invincible_framenum = 0
            ent.client.breather_framenum = 0
            ent.client.enviro_framenum = 0
            ent.client.grenade_blew_up = false
            ent.client.grenade_time = 0

            ent.viewheight = 0
            ent.s.modelindex = 0
            ent.s.modelindex2 = 0
            ent.s.modelindex3 = 0
            ent.s.modelindex = 0
            ent.s.effects = 0
            ent.s.sound = 0
            ent.solid = Defines.SOLID_NOT

            // add the layout

            if (GameBase.deathmatch.value != 0 || GameBase.coop.value != 0) {
                DeathmatchScoreboardMessage(ent, null)
                GameBase.gi.unicast(ent, true)
            }

        }

        public fun BeginIntermission(targ: edict_t) {
            var i: Int
            var n: Int
            var ent: edict_t?
            var client: edict_t

            if (GameBase.level.intermissiontime != 0)
                return  // already activated

            GameBase.game.autosaved = false

            // respawn any dead clients
            run {
                i = 0
                while (i < GameBase.maxclients.value) {
                    client = GameBase.g_edicts[1 + i]
                    if (!client.inuse)
                        continue
                    if (client.health <= 0)
                        PlayerClient.respawn(client)
                    i++
                }
            }

            GameBase.level.intermissiontime = GameBase.level.time
            GameBase.level.changemap = targ.map

            if (GameBase.level.changemap.indexOf('*') > -1) {
                if (GameBase.coop.value != 0) {
                    run {
                        i = 0
                        while (i < GameBase.maxclients.value) {
                            client = GameBase.g_edicts[1 + i]
                            if (!client.inuse)
                                continue
                            // strip players of all keys between units
                            run {
                                n = 1
                                while (n < GameItemList.itemlist.length) {
                                    // null pointer exception fixed. (RST)
                                    if (GameItemList.itemlist[n] != null)
                                        if ((GameItemList.itemlist[n].flags and Defines.IT_KEY) != 0)
                                            client.client.pers.inventory[n] = 0
                                    n++
                                }
                            }
                            i++
                        }
                    }
                }
            } else {
                if (0 == GameBase.deathmatch.value) {
                    GameBase.level.exitintermission = true // go immediately to the
                    // next level
                    return
                }
            }

            GameBase.level.exitintermission = false

            // find an intermission spot
            ent = GameBase.G_FindEdict(null, GameBase.findByClass, "info_player_intermission")
            if (ent == null) {
                // the map creator forgot to put in an intermission
                // point...
                ent = GameBase.G_FindEdict(null, GameBase.findByClass, "info_player_start")
                if (ent == null)
                    ent = GameBase.G_FindEdict(null, GameBase.findByClass, "info_player_deathmatch")
            } else {
                // chose one of four spots
                i = Lib.rand() and 3
                var es: EdictIterator? = null

                while (i-- > 0) {
                    es = GameBase.G_Find(es, GameBase.findByClass, "info_player_intermission")

                    if (es == null)
                    // wrap around the list
                        continue
                    ent = es!!.o
                }
            }

            Math3D.VectorCopy(ent!!.s.origin, GameBase.level.intermission_origin)
            Math3D.VectorCopy(ent!!.s.angles, GameBase.level.intermission_angle)

            // move all clients to the intermission point
            run {
                i = 0
                while (i < GameBase.maxclients.value) {
                    client = GameBase.g_edicts[1 + i]
                    if (!client.inuse)
                        continue
                    MoveClientToIntermission(client)
                    i++
                }
            }
        }

        /*
     * ================== 
     * DeathmatchScoreboardMessage
     * ==================
     */
        public fun DeathmatchScoreboardMessage(ent: edict_t, killer: edict_t?) {
            val string = StringBuffer(1400)

            var i: Int
            var j: Int
            var k: Int
            val sorted = IntArray(Defines.MAX_CLIENTS)
            val sortedscores = IntArray(Defines.MAX_CLIENTS)
            var score: Int
            var total: Int
            var x: Int
            var y: Int
            var cl: gclient_t
            var cl_ent: edict_t
            var tag: String?

            // sort the clients by score
            total = 0
            run {
                i = 0
                while (i < GameBase.game.maxclients) {
                    cl_ent = GameBase.g_edicts[1 + i]
                    if (!cl_ent.inuse || GameBase.game.clients[i].resp.spectator)
                        continue
                    score = GameBase.game.clients[i].resp.score
                    run {
                        j = 0
                        while (j < total) {
                            if (score > sortedscores[j])
                                break
                            j++
                        }
                    }
                    run {
                        k = total
                        while (k > j) {
                            sorted[k] = sorted[k - 1]
                            sortedscores[k] = sortedscores[k - 1]
                            k--
                        }
                    }
                    sorted[j] = i
                    sortedscores[j] = score
                    total++
                    i++
                }
            }

            // print level name and exit rules

            // add the clients in sorted order
            if (total > 12)
                total = 12

            run {
                i = 0
                while (i < total) {
                    cl = GameBase.game.clients[sorted[i]]
                    cl_ent = GameBase.g_edicts[1 + sorted[i]]

                    GameBase.gi.imageindex("i_fixme")
                    x = if ((i >= 6)) 160 else 0
                    y = 32 + 32 * (i % 6)

                    // add a dogtag
                    if (cl_ent == ent)
                        tag = "tag1"
                    else if (cl_ent == killer)
                        tag = "tag2"
                    else
                        tag = null

                    if (tag != null) {
                        string.append("xv ").append(x + 32).append(" yv ").append(y).append(" picn ").append(tag)
                    }

                    // send the layout
                    string.append(" client ").append(x).append(" ").append(y).append(" ").append(sorted[i]).append(" ").append(cl.resp.score).append(" ").append(cl.ping).append(" ").append((GameBase.level.framenum - cl.resp.enterframe) / 600)
                    i++
                }
            }

            GameBase.gi.WriteByte(Defines.svc_layout)
            GameBase.gi.WriteString(string.toString())
        }

        /*
     * ================== 
     * DeathmatchScoreboard
     * 
     * Draw instead of help message. Note that it isn't that hard to overflow
     * the 1400 byte message limit! 
     * ==================
     */
        public fun DeathmatchScoreboard(ent: edict_t) {
            DeathmatchScoreboardMessage(ent, ent.enemy)
            GameBase.gi.unicast(ent, true)
        }

        /*
     * ================== 
     * Cmd_Score_f
     * 
     * Display the scoreboard 
     * ==================
     */
        public fun Cmd_Score_f(ent: edict_t) {
            ent.client.showinventory = false
            ent.client.showhelp = false

            if (0 == GameBase.deathmatch.value && 0 == GameBase.coop.value)
                return

            if (ent.client.showscores) {
                ent.client.showscores = false
                return
            }

            ent.client.showscores = true
            DeathmatchScoreboard(ent)
        }

        //=======================================================================

        /*
     * =============== 
     * G_SetStats 
     * ===============
     */
        public fun G_SetStats(ent: edict_t) {
            val item: gitem_t
            val index: Int
            var cells = 0
            var power_armor_type: Int

            //
            // health
            //
            ent.client.ps.stats[Defines.STAT_HEALTH_ICON] = GameBase.level.pic_health as Short
            ent.client.ps.stats[Defines.STAT_HEALTH] = ent.health as Short

            //
            // ammo
            //
            if (0 == ent.client.ammo_index /*
                                        * ||
                                        * !ent.client.pers.inventory[ent.client.ammo_index]
                                        */) {
                ent.client.ps.stats[Defines.STAT_AMMO_ICON] = 0
                ent.client.ps.stats[Defines.STAT_AMMO] = 0
            } else {
                item = GameItemList.itemlist[ent.client.ammo_index]
                ent.client.ps.stats[Defines.STAT_AMMO_ICON] = GameBase.gi.imageindex(item.icon) as Short
                ent.client.ps.stats[Defines.STAT_AMMO] = ent.client.pers.inventory[ent.client.ammo_index] as Short
            }

            //
            // armor
            //
            power_armor_type = GameItems.PowerArmorType(ent)
            if (power_armor_type != 0) {
                cells = ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("cells"))]
                if (cells == 0) {
                    // ran out of cells for power armor
                    ent.flags = ent.flags and Defines.FL_POWER_ARMOR.inv()
                    GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("misc/power2.wav"), 1, Defines.ATTN_NORM, 0)
                    power_armor_type = 0
                }
            }

            index = GameItems.ArmorIndex(ent)
            if (power_armor_type != 0 && (0 == index || 0 != (GameBase.level.framenum and 8))) {
                // flash
                // between
                // power
                // armor
                // and
                // other
                // armor
                // icon
                ent.client.ps.stats[Defines.STAT_ARMOR_ICON] = GameBase.gi.imageindex("i_powershield") as Short
                ent.client.ps.stats[Defines.STAT_ARMOR] = cells.toShort()
            } else if (index != 0) {
                item = GameItems.GetItemByIndex(index)
                ent.client.ps.stats[Defines.STAT_ARMOR_ICON] = GameBase.gi.imageindex(item.icon) as Short
                ent.client.ps.stats[Defines.STAT_ARMOR] = ent.client.pers.inventory[index] as Short
            } else {
                ent.client.ps.stats[Defines.STAT_ARMOR_ICON] = 0
                ent.client.ps.stats[Defines.STAT_ARMOR] = 0
            }

            //
            // pickup message
            //
            if (GameBase.level.time > ent.client.pickup_msg_time) {
                ent.client.ps.stats[Defines.STAT_PICKUP_ICON] = 0
                ent.client.ps.stats[Defines.STAT_PICKUP_STRING] = 0
            }

            //
            // timers
            //
            if (ent.client.quad_framenum > GameBase.level.framenum) {
                ent.client.ps.stats[Defines.STAT_TIMER_ICON] = GameBase.gi.imageindex("p_quad") as Short
                ent.client.ps.stats[Defines.STAT_TIMER] = ((ent.client.quad_framenum - GameBase.level.framenum) / 10) as Short
            } else if (ent.client.invincible_framenum > GameBase.level.framenum) {
                ent.client.ps.stats[Defines.STAT_TIMER_ICON] = GameBase.gi.imageindex("p_invulnerability") as Short
                ent.client.ps.stats[Defines.STAT_TIMER] = ((ent.client.invincible_framenum - GameBase.level.framenum) / 10) as Short
            } else if (ent.client.enviro_framenum > GameBase.level.framenum) {
                ent.client.ps.stats[Defines.STAT_TIMER_ICON] = GameBase.gi.imageindex("p_envirosuit") as Short
                ent.client.ps.stats[Defines.STAT_TIMER] = ((ent.client.enviro_framenum - GameBase.level.framenum) / 10) as Short
            } else if (ent.client.breather_framenum > GameBase.level.framenum) {
                ent.client.ps.stats[Defines.STAT_TIMER_ICON] = GameBase.gi.imageindex("p_rebreather") as Short
                ent.client.ps.stats[Defines.STAT_TIMER] = ((ent.client.breather_framenum - GameBase.level.framenum) / 10) as Short
            } else {
                ent.client.ps.stats[Defines.STAT_TIMER_ICON] = 0
                ent.client.ps.stats[Defines.STAT_TIMER] = 0
            }

            //
            // selected item
            //
            // bugfix rst
            if (ent.client.pers.selected_item <= 0)
                ent.client.ps.stats[Defines.STAT_SELECTED_ICON] = 0
            else
                ent.client.ps.stats[Defines.STAT_SELECTED_ICON] = GameBase.gi.imageindex(GameItemList.itemlist[ent.client.pers.selected_item].icon) as Short

            ent.client.ps.stats[Defines.STAT_SELECTED_ITEM] = ent.client.pers.selected_item as Short

            //
            // layouts
            //
            ent.client.ps.stats[Defines.STAT_LAYOUTS] = 0

            if (GameBase.deathmatch.value != 0) {
                if (ent.client.pers.health <= 0 || GameBase.level.intermissiontime != 0 || ent.client.showscores)
                    ent.client.ps.stats[Defines.STAT_LAYOUTS] = ent.client.ps.stats[Defines.STAT_LAYOUTS] or 1
                if (ent.client.showinventory && ent.client.pers.health > 0)
                    ent.client.ps.stats[Defines.STAT_LAYOUTS] = ent.client.ps.stats[Defines.STAT_LAYOUTS] or 2
            } else {
                if (ent.client.showscores || ent.client.showhelp)
                    ent.client.ps.stats[Defines.STAT_LAYOUTS] = ent.client.ps.stats[Defines.STAT_LAYOUTS] or 1
                if (ent.client.showinventory && ent.client.pers.health > 0)
                    ent.client.ps.stats[Defines.STAT_LAYOUTS] = ent.client.ps.stats[Defines.STAT_LAYOUTS] or 2
            }

            //
            // frags
            //
            ent.client.ps.stats[Defines.STAT_FRAGS] = ent.client.resp.score as Short

            //
            // help icon / current weapon if not shown
            //
            if (ent.client.pers.helpchanged != 0 && (GameBase.level.framenum and 8) != 0)
                ent.client.ps.stats[Defines.STAT_HELPICON] = GameBase.gi.imageindex("i_help") as Short
            else if ((ent.client.pers.hand == Defines.CENTER_HANDED || ent.client.ps.fov > 91) && ent.client.pers.weapon != null)
                ent.client.ps.stats[Defines.STAT_HELPICON] = GameBase.gi.imageindex(ent.client.pers.weapon.icon) as Short
            else
                ent.client.ps.stats[Defines.STAT_HELPICON] = 0

            ent.client.ps.stats[Defines.STAT_SPECTATOR] = 0
        }

        /*
     * =============== 
     * G_CheckChaseStats 
     * ===============
     */
        public fun G_CheckChaseStats(ent: edict_t) {
            var i: Int
            var cl: gclient_t

            run {
                i = 1
                while (i <= GameBase.maxclients.value) {
                    cl = GameBase.g_edicts[i].client
                    if (!GameBase.g_edicts[i].inuse || cl.chase_target != ent)
                        continue
                    //memcpy(cl.ps.stats, ent.client.ps.stats, sizeof(cl.ps.stats));
                    System.arraycopy(ent.client.ps.stats, 0, cl.ps.stats, 0, Defines.MAX_STATS)

                    G_SetSpectatorStats(GameBase.g_edicts[i])
                    i++
                }
            }
        }

        /*
     * =============== 
     * G_SetSpectatorStats 
     * ===============
     */
        public fun G_SetSpectatorStats(ent: edict_t) {
            val cl = ent.client

            if (null == cl.chase_target)
                G_SetStats(ent)

            cl.ps.stats[Defines.STAT_SPECTATOR] = 1

            // layouts are independant in spectator
            cl.ps.stats[Defines.STAT_LAYOUTS] = 0
            if (cl.pers.health <= 0 || GameBase.level.intermissiontime != 0 || cl.showscores)
                cl.ps.stats[Defines.STAT_LAYOUTS] = cl.ps.stats[Defines.STAT_LAYOUTS] or 1
            if (cl.showinventory && cl.pers.health > 0)
                cl.ps.stats[Defines.STAT_LAYOUTS] = cl.ps.stats[Defines.STAT_LAYOUTS] or 2

            if (cl.chase_target != null && cl.chase_target.inuse)
            //cl.ps.stats[STAT_CHASE] = (short) (CS_PLAYERSKINS +
            // (cl.chase_target - g_edicts) - 1);
                cl.ps.stats[Defines.STAT_CHASE] = (Defines.CS_PLAYERSKINS + cl.chase_target.index - 1) as Short
            else
                cl.ps.stats[Defines.STAT_CHASE] = 0
        }

        /**
         * HelpComputer. Draws the help computer.
         */
        public fun HelpComputer(ent: edict_t) {
            val sb = StringBuffer(256)
            val sk: String

            if (GameBase.skill.value == 0)
                sk = "easy"
            else if (GameBase.skill.value == 1)
                sk = "medium"
            else if (GameBase.skill.value == 2)
                sk = "hard"
            else
                sk = "hard+"

            // send the layout
            sb.append("xv 32 yv 8 picn help ") // background
            sb.append("xv 202 yv 12 string2 \"").append(sk).append("\" ") // skill
            sb.append("xv 0 yv 24 cstring2 \"").append(GameBase.level.level_name).append("\" ") // level name
            sb.append("xv 0 yv 54 cstring2 \"").append(GameBase.game.helpmessage1).append("\" ") // help 1
            sb.append("xv 0 yv 110 cstring2 \"").append(GameBase.game.helpmessage2).append("\" ") // help 2
            sb.append("xv 50 yv 164 string2 \" kills     goals    secrets\" ")
            sb.append("xv 50 yv 172 string2 \"")
            sb.append(Com.sprintf("%3i/%3i     %i/%i       %i/%i\" ", Vargs(6).add(GameBase.level.killed_monsters).add(GameBase.level.total_monsters).add(GameBase.level.found_goals).add(GameBase.level.total_goals).add(GameBase.level.found_secrets).add(GameBase.level.total_secrets)))

            GameBase.gi.WriteByte(Defines.svc_layout)
            GameBase.gi.WriteString(sb.toString())
            GameBase.gi.unicast(ent, true)
        }
    }
}